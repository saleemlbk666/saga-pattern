package com.saga.PaymentService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReversePayment {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @KafkaListener(topics = "reverse-payments", groupId = "payments-group")
    public void reversePayment(String event){
        System.out.println("Inside reverse payment for order: "+event);

        try{
            PaymentEvent paymentEvent = new ObjectMapper().readValue(event, PaymentEvent.class);
            CustomerOrder customerOrder = paymentEvent.getCustomerOrder();

            List<Payment> payments = this.paymentRepository.findByOrderId(customerOrder.getOrderId());

            payments.forEach(payment -> {
                payment.setStatus("Failed");
                paymentRepository.save(payment);
            });

            OrderEvent orderEvent = new OrderEvent();
            orderEvent.setCustomerOrder(customerOrder);
            orderEvent.setType("ORDER_REVERSED");
            kafkaTemplate.send("reverse-orders", orderEvent);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

}
