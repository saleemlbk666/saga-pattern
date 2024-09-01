package com.saga.StockService;

import lombok.Data;

@Data
public class PaymentEvent {
    private String type;

    private CustomerOrder customerOrder;
}
