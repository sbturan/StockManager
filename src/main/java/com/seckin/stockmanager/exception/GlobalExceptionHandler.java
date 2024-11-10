package com.seckin.stockmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<String> handleValidationException(jakarta.validation.ConstraintViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Validation failed: " + ex.getMessage());
    }

    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<String> handleBindException(BindingResult result) {
        StringBuilder sb = new StringBuilder("Validation failed: ");
        result.getAllErrors().forEach(error -> sb.append(error.getDefaultMessage()).append(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(sb.toString());
    }
    @ExceptionHandler(AssetUsableSizeNotEnoughException.class)
    public ResponseEntity<String> handleAssetUsableSizeNotEnoughException(AssetUsableSizeNotEnoughException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
    @ExceptionHandler(CustomerExistsException.class)
    public ResponseEntity<String> handleCustomerExistsException(CustomerExistsException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
