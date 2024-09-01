package com.saga.StockService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StockController {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private KafkaTemplate<String, PaymentEvent> paymentKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, DeliveryEvent> deliveryKafkaTemplate;

    @KafkaListener(topics = "new-payments", groupId = "payments-group")
    public void updateStock(String event) throws JsonProcessingException {
        System.out.println("Inside Update inventory for order: "+ event);

        DeliveryEvent deliveryEvent = new DeliveryEvent();

        PaymentEvent paymentEvent = new ObjectMapper().readValue(event, PaymentEvent.class);

        CustomerOrder order = paymentEvent.getCustomerOrder();

        try{
            Iterable<WareHouse> inventories = stockRepository.findByItem(order.getItem());

            boolean exists = inventories.iterator().hasNext();

            if(!exists){
                System.out.println("Out of Stock, hence reverting the order "+event);
                throw new Exception("Stock not available");
            }

            inventories.forEach(i -> {
                i.setQuantity(i.getQuantity() - order.getQuantity());
                stockRepository.save(i);
            });

            deliveryEvent.setType("STOCK_UPDATED");
            deliveryEvent.setCustomerOrder(order);

            deliveryKafkaTemplate.send("new-stock", deliveryEvent);
        }
        catch (Exception e){
            PaymentEvent reversePaymentEvent = new PaymentEvent();
            reversePaymentEvent.setCustomerOrder(paymentEvent.getCustomerOrder());
            reversePaymentEvent.setType("PAYMENT_REVERSED");
            paymentKafkaTemplate.send("reverse-payments", reversePaymentEvent);
        }
    }

    @PostMapping("/addItems")
    public void addItems(@RequestBody Stock stock){

        Iterable<WareHouse> items = stockRepository.findByItem(stock.getItem());

        if(items.iterator().hasNext()){
            items.forEach(i -> {
                i.setQuantity(stock.getQuantity() + i.getQuantity());
                stockRepository.save(i);
            });
        }
        else {
            WareHouse wareHouse = new WareHouse();
            wareHouse.setItem(stock.getItem());
            wareHouse.setQuantity(stock.getQuantity());
            stockRepository.save(wareHouse);
        }
    }
}
