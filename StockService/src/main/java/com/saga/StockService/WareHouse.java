package com.saga.StockService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class WareHouse {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private int quantity;

    @Column
    private String item;
}
