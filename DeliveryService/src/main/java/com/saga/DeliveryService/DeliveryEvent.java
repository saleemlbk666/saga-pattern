package com.saga.DeliveryService;

import lombok.Data;

@Data
public class DeliveryEvent {

    private String type;

    private CustomerOrder customerOrder;
}
