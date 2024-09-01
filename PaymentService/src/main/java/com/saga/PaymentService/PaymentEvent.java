package com.saga.PaymentService;

import lombok.Data;

@Data
public class PaymentEvent {

    private CustomerOrder  customerOrder;

    private String type;
}
