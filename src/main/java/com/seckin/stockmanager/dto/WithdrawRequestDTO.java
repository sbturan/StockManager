package com.seckin.stockmanager.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class WithdrawRequestDTO {
    @NotNull(message = "customerName must not be null")
    public String customerUserName;
    @NotNull(message = "The amount must not be null")
    @Positive(message = "The amount must be greater than 0")
    public Double amount;

    @NotNull(message = "IBAN must not be null")
    public String iban;
}
