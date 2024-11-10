package com.seckin.stockmanager.controller;

import com.seckin.stockmanager.dto.ListOrderRequestDTO;
import com.seckin.stockmanager.dto.OrderDTO;
import com.seckin.stockmanager.service.CustomerService;
import com.seckin.stockmanager.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final CustomerService customerService;

    public OrderController(OrderService orderService,CustomerService customerService) {
        this.orderService = orderService;
        this.customerService=customerService;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO order, Authentication authentication) {
        customerService.validateUserAuthenticated(order.customerUserName,authentication);
        OrderDTO createdOrderDto = orderService.createOrder(order);
        return new ResponseEntity<>(createdOrderDto, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<OrderDTO>> listOrders(@Valid @RequestBody ListOrderRequestDTO listOrderRequestDTO,Authentication authentication) {
        customerService.validateUserAuthenticated(listOrderRequestDTO.customerUserName,authentication);
        return new ResponseEntity<>(orderService.listOrders(listOrderRequestDTO),
                HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId,Authentication authentication) {
        orderService.deleteOrder(orderId,authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/match/{orderId}")
    public ResponseEntity<Void> matchOrder(@PathVariable Long orderId){
        orderService.matchOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
