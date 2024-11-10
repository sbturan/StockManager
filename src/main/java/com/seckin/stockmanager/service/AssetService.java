package com.seckin.stockmanager.service;

import com.seckin.stockmanager.dto.AssetDTO;
import com.seckin.stockmanager.exception.AssetUsableSizeNotEnoughException;
import com.seckin.stockmanager.model.Asset;
import com.seckin.stockmanager.repository.AssetRepository;
import jakarta.persistence.PessimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.seckin.stockmanager.service.Constants.TRY_ASSET_NAME;

@Service
public class AssetService {
    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);


    private final AssetRepository assetRepository;
    private final CustomerService customerService;


    public AssetService(AssetRepository assetRepository,
                        CustomerService customerService) {
        this.assetRepository = assetRepository;
        this.customerService = customerService;
    }

    public Asset getAssetWithLock(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndName(customerId, assetName).orElse(null);
    }

    public Asset saveAsset(Asset asset) {
        try {
            return assetRepository.save(asset);
        } catch (PessimisticLockException e) {
            logger.error("PessimisticLockException");
            throw new RuntimeException("The record was updated by another transaction",
                    e);
        }
    }

    @Transactional
    public void depositMoney(String customerUserName, Double amount) {
        long customerId = customerService.getCustomerId(customerUserName);
        Asset tryAsset = getAssetWithLock(customerId, TRY_ASSET_NAME);
        if (tryAsset == null) {
            tryAsset = new Asset(customerId, TRY_ASSET_NAME, 0d, 0d);
        }
        tryAsset.setSize(tryAsset.getSize() + amount);
        tryAsset.setUsableSize(tryAsset.getUsableSize() + amount);
        saveAsset(tryAsset);
    }

    @Transactional
    public void withdrawMoney(String customerUserName, Double amount, String iban) {
        long customerId = customerService.getCustomerId(customerUserName);
        Asset tryAsset = getAssetWithLock(customerId, TRY_ASSET_NAME);
        if (tryAsset == null || tryAsset.getUsableSize().compareTo(amount) < 0) {
            logger.error("Insufficient Asset Usable Size for customer:"+customerUserName);
            throw new AssetUsableSizeNotEnoughException("Insufficient Asset Usable Size");
        }
        tryAsset.setUsableSize(tryAsset.getUsableSize() - amount);
        tryAsset.setSize(tryAsset.getSize() - amount);
        saveAsset(tryAsset);
        sendAmountToIban(customerId, amount, iban);
    }

    public void sendAmountToIban(long customerId, Double amount, String iban) {
        // call send to iban service
    }

    public List<AssetDTO> listAssets(String customerUserName, String assetName) {
        long customerId = customerService.getCustomerId(customerUserName);
        return assetRepository.findAssets(customerId, assetName)
                .stream().map(it -> new AssetDTO(it, customerUserName)).toList();
    }
}
