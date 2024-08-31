package com.saga.OrderService;

import lombok.Data;

@Data
public class OrderEvent {

    private CustomerOrder customerOrder;

    private String type;
}
