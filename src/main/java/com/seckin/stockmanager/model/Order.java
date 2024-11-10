package com.seckin.stockmanager.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private long customerId;
    @Column(nullable = false)
    private String assetName;
    @Column(nullable = false)
    private OrderSide orderSide;
    @Column(nullable = false)
    private Double prize;
    @Column(nullable = false)
    private Double size;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createDate;

    public Order() {
    }

    public Order(long customerId, String assetName, OrderSide orderSide, Double prize,
                 Double size, OrderStatus status) {
        this.customerId = customerId;
        this.assetName = assetName;
        this.orderSide = orderSide;
        this.prize = prize;
        this.size = size;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public void setOrderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
    }

    public Double getPrize() {
        return prize;
    }

    public void setPrize(Double prize) {
        this.prize = prize;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Instant createDate) {
        this.createDate = createDate;
    }
}
