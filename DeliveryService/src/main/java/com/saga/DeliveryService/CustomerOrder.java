package com.saga.DeliveryService;

import lombok.Data;

@Data
public class CustomerOrder {
    private String item;

    private int quantity;

    private double amount;

    private String paymentMethod;

    private Long orderId;

    private String address;
}
