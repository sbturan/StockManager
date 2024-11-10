package com.seckin.stockmanager.exception;

public class CustomerExistsException extends RuntimeException{
    public CustomerExistsException(String message) {
        super(message);
    }
}
