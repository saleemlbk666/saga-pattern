package com.saga.OrderService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ReverseOrder {

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(topics = "reversed-orders", groupId = "orders-group")
    public void reverseOrder(String event){
        System.out.println("Reverse Order Event: "+event);

        try{
            OrderEvent orderEvent = new ObjectMapper().readValue(event, OrderEvent.class);
            Optional<Order> orderOptional = orderRepository.findById(orderEvent.getCustomerOrder().getOrderId());
            if (orderOptional.isPresent()){
                Order order = orderOptional.get();
                order.setStatus("Failed");
                orderRepository.save(order);
            }
        }
        catch (final Exception e){
            System.out.println("Exception Occured while reverting order details");
        }
    }
}
