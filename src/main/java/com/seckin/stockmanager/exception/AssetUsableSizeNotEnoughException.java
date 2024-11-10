package com.seckin.stockmanager.exception;

public class AssetUsableSizeNotEnoughException extends RuntimeException {
    public AssetUsableSizeNotEnoughException(String message){
        super(message);
    }
}
