package com.seckin.stockmanager.dto;

import jakarta.validation.constraints.NotNull;

public class ListAssetRequestDTO {

    @NotNull(message = "customerName must not be null")
    public String customerUserName;
    public String assetName;
}
