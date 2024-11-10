package com.seckin.stockmanager.dto;

import com.seckin.stockmanager.model.Asset;

public class AssetDTO {
    public Long id;

    public String customerUserName;

    public String name;

    public Double size;

    public Double usableSize;

    public AssetDTO(Asset asset,String customerUserName){
        this.id=asset.getId();
        this.customerUserName=customerUserName;
        this.name= asset.getName();;
        this.size= asset.getSize();;
        this.usableSize= asset.getUsableSize();
    }
}
