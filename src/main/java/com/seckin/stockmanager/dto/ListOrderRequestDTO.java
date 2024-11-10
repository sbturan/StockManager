package com.seckin.stockmanager.dto;

import com.seckin.stockmanager.model.OrderSide;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ListOrderRequestDTO {
    @NotNull(message = "customerName must not be null")
    public String customerUserName;
    public Instant minDate;
    public Instant maxDate;
    public OrderSide orderSide;
    public String assetName;
}
