package com.saga.PaymentService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, PaymentEvent> paymentKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, OrderEvent> orderKafkaTemplate;

    @KafkaListener(topics = "new-orders", groupId = "orders-group")
    public void processPayment(String event) throws JsonProcessingException {

        System.out.println("Process payment event: "+event);

        OrderEvent orderEvent = new ObjectMapper().readValue(event, OrderEvent.class);

        Payment payment = new Payment();

        try{
            payment.setAmount(orderEvent.getCustomerOrder().getAmount());
            payment.setMode(orderEvent.getCustomerOrder().getPaymentMethod());
            payment.setOrderId(orderEvent.getCustomerOrder().getOrderId());
            payment.setStatus("Success");
            paymentRepository.save(payment);
            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setCustomerOrder(orderEvent.getCustomerOrder());
            paymentEvent.setType("PAYMENT_CREATED");

            paymentKafkaTemplate.send("new-payments", paymentEvent);
        }
        catch (Exception e){
            payment.setStatus("Failed");
            paymentRepository.save(payment);

            OrderEvent failedOrderEvent = new OrderEvent();
            failedOrderEvent.setCustomerOrder(orderEvent.getCustomerOrder());
            failedOrderEvent.setType("ORDER_REVERSED");
            orderKafkaTemplate.send("reverse-orders", failedOrderEvent);
        }
    }
}
