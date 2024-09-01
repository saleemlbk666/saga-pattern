package com.saga.StockService;

import lombok.Data;

@Data
public class DeliveryEvent {
    private String type;

    private CustomerOrder customerOrder;
}
