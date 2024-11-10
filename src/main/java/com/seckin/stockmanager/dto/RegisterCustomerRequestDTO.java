package com.seckin.stockmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterCustomerRequestDTO {
    @NotBlank
    public String username;
    @NotBlank
    public String password;
}
