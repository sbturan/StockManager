package com.seckin.stockmanager.dto;

import com.seckin.stockmanager.model.Order;
import com.seckin.stockmanager.model.OrderSide;
import com.seckin.stockmanager.model.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public class OrderDTO {
    public long id;
    @NotNull(message = "customerName must not be null")
    public String customerUserName;
    @NotBlank(message = "AssetName can not be blank")
    public String assetName;
    @NotNull(message = "Invalid Side value")
    public OrderSide side;
    @NotNull(message = "The amount must not be null")
    @Positive(message = "The amount must be greater than 0")
    public Double orderSize;
    @NotNull(message = "Prize can not be null")
    public Double prize;
    public OrderStatus status;
    public Instant createdDate;

    public OrderDTO() {
    }

    public Order toOrder(long customerId) {
        return new Order(customerId, assetName, side, prize, orderSize,
                OrderStatus.PENDING);
    }

    public OrderDTO(Order order,String customerUserName) {
        this.id = order.getId();
        this.customerUserName = customerUserName;
        this.assetName = order.getAssetName();
        this.side = order.getOrderSide();
        this.orderSize = order.getSize();
        this.prize = order.getPrize();
        this.status = order.getStatus();
        this.createdDate = order.getCreateDate();
    }

}
