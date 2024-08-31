package com.saga.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;


    @PostMapping
    public void createOrder(@RequestBody CustomerOrder customerOrder){

        Order order = new Order();

        order.setAmount(customerOrder.getAmount());
        order.setItem(customerOrder.getItem());
        order.setQuantity(customerOrder.getQuantity());
        order.setStatus("Order Created");

        try{
            order = orderRepository.save(order);
            customerOrder.setOrderId(order.getId());

            OrderEvent orderEvent = new OrderEvent();
            orderEvent.setCustomerOrder(customerOrder);
            orderEvent.setType("ORDER_CREATED");

            kafkaTemplate.send("new-orders", orderEvent);
        }catch (Exception e){
            order.setStatus("Order Failed");
            orderRepository.save(order);
        }


    }
}
