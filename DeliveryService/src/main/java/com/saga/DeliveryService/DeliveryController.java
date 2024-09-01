package com.saga.DeliveryService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class DeliveryController {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private KafkaTemplate<String, DeliveryEvent> deliveryKafkaTemplate;

    @KafkaListener(topics = "new-stock", groupId = "stock-group")
    public void deliverOrder(String event) throws JsonProcessingException {
        System.out.println("Inside ship order for the order "+event);

        Delivery delivery = new Delivery();
        DeliveryEvent deliveryEvent = new ObjectMapper().readValue(event, DeliveryEvent.class);

        CustomerOrder customerOrder = deliveryEvent.getCustomerOrder();

        try{
            if(customerOrder.getAddress() == null){
                throw new Exception("Address Not found");
            }
            delivery.setAddress(customerOrder.getAddress());
            delivery.setOrderId(customerOrder.getOrderId());
            delivery.setStatus("Success");

            deliveryRepository.save(delivery);
        }
        catch (Exception e){
            delivery.setOrderId(customerOrder.getOrderId());
            delivery.setStatus("Failed");

            deliveryRepository.save(delivery);

            DeliveryEvent reverseDeliveryEvent = new DeliveryEvent();

            reverseDeliveryEvent.setType("STOCK_REVERSED");
            reverseDeliveryEvent.setCustomerOrder(customerOrder);
            deliveryKafkaTemplate.send("rverse-stock", reverseDeliveryEvent);
        }
    }
}
