package com.seckin.stockmanager.controller;

import com.seckin.stockmanager.dto.CustomerDTO;
import com.seckin.stockmanager.dto.RegisterCustomerRequestDTO;
import com.seckin.stockmanager.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerDTO> register(@Valid @RequestBody RegisterCustomerRequestDTO customer) {
        customerService.addNewCustomer(customer.username,customer.password);
        return  ResponseEntity.ok().build();
    }
}
