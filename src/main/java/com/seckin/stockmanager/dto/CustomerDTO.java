package com.seckin.stockmanager.dto;

public class CustomerDTO {
    public Long id;
    public String username;

    public CustomerDTO(Long id,String username){
        this.id=id;
        this.username=username;
    }
}
