package com.saga.StockService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReverseStock {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @KafkaListener(topics = "reverse-stock", groupId = "stock-group")
    public void reverseStock(String event) {
        System.out.println("Inside reverse stock for order " + event);

        try {
            DeliveryEvent deliveryEvent = new ObjectMapper().readValue(event, DeliveryEvent.class);
            Iterable<WareHouse> inventory = this.stockRepository.findByItem(deliveryEvent.getCustomerOrder().getItem());
            inventory.forEach(i -> {
                i.setQuantity(i.getQuantity() + deliveryEvent.getCustomerOrder().getQuantity());
                stockRepository.save(i);
            });

            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setCustomerOrder(deliveryEvent.getCustomerOrder());
            paymentEvent.setType("PAYMENT_REVERSED");
            kafkaTemplate.send("reversed-payments", paymentEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
