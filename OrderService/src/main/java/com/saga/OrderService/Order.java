package com.saga.OrderService;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private String item;

    @Column
    private int quantity;

    @Column
    private double amount;

    @Column
    private String status;

}
