package com.saga.DeliveryService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Delivery {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private String address;

    @Column
    private String status;

    @Column
    private long orderId;
}
