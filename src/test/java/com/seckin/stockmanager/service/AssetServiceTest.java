package com.seckin.stockmanager.service;

import com.seckin.stockmanager.dto.AssetDTO;
import com.seckin.stockmanager.exception.AssetUsableSizeNotEnoughException;
import com.seckin.stockmanager.model.Asset;
import com.seckin.stockmanager.repository.AssetRepository;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static com.seckin.stockmanager.service.Constants.TRY_ASSET_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAssetWithLock_ShouldReturnAsset_WhenAssetExists() {
        Long customerId = 1L;
        String assetName = "AAPL";
        Asset asset = new Asset(customerId, assetName, 100d, 100d);

        when(assetRepository.findByCustomerIdAndName(customerId, assetName)).thenReturn(Optional.of(asset));

        Asset result = assetService.getAssetWithLock(customerId, assetName);

        assertNotNull(result);
        assertEquals(asset, result);
        verify(assetRepository, times(1)).findByCustomerIdAndName(customerId, assetName);
    }

    @Test
    void getAssetWithLock_ShouldReturnNull_WhenAssetDoesNotExist() {
        Long customerId = 1L;
        String assetName = "AAPL";

        when(assetRepository.findByCustomerIdAndName(customerId, assetName)).thenReturn(Optional.empty());

        Asset result = assetService.getAssetWithLock(customerId, assetName);

        assertNull(result);
        verify(assetRepository, times(1)).findByCustomerIdAndName(customerId, assetName);
    }

    @Test
    void saveAsset_ShouldSaveAsset_WhenNoLockException() {
        Asset asset = new Asset(1L, "AAPL", 100d, 100d);

        when(assetRepository.save(asset)).thenReturn(asset);

        Asset result = assetService.saveAsset(asset);

        assertEquals(asset, result);
        verify(assetRepository, times(1)).save(asset);
    }

    @Test
    void saveAsset_ShouldThrowRuntimeException_WhenPessimisticLockException() {
        Asset asset = new Asset(1L, "AAPL", 100d, 100d);

        when(assetRepository.save(asset)).thenThrow(new PessimisticLockException());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> assetService.saveAsset(asset));

        assertEquals("The record was updated by another transaction", exception.getMessage());
        verify(assetRepository, times(1)).save(asset);
    }

    @Test
    void depositMoney_ShouldIncreaseUsableSizeAndSize_WhenAssetExists() {
        String customerUserName = "testUser";
        Double amount = 50.0;
        long customerId = 1L;
        Asset tryAsset = new Asset(customerId, TRY_ASSET_NAME, 100.0, 100.0);

        when(customerService.getCustomerId(customerUserName)).thenReturn(customerId);
        when(assetRepository.findByCustomerIdAndName(customerId, TRY_ASSET_NAME)).thenReturn(Optional.of(tryAsset));

        assetService.depositMoney(customerUserName, amount);

        assertEquals(150.0, tryAsset.getSize());
        assertEquals(150.0, tryAsset.getUsableSize());
        verify(assetRepository, times(1)).save(tryAsset);
    }

    @Test
    void depositMoney_ShouldCreateAsset_WhenAssetDoesNotExist() {
        String customerUserName = "testUser";
        Double amount = 50.0;
        long customerId = 1L;

        when(customerService.getCustomerId(customerUserName)).thenReturn(customerId);
        when(assetRepository.findByCustomerIdAndName(customerId, TRY_ASSET_NAME)).thenReturn(Optional.empty());

        assetService.depositMoney(customerUserName, amount);

        verify(assetRepository, times(1)).save(argThat(asset ->
                asset.getSize().equals(amount) &&
                        asset.getUsableSize().equals(amount) &&
                        asset.getCustomerId()==customerId &&
                        asset.getName().equals(TRY_ASSET_NAME)
        ));
    }

    @Test
    void withdrawMoney_ShouldDecreaseUsableSizeAndSize_WhenSufficientUsableSize() {
        String customerUserName = "testUser";
        Double amount = 50.0;
        String iban = "TR000000000000000000000000";
        long customerId = 1L;
        Asset tryAsset = new Asset(customerId, TRY_ASSET_NAME, 100.0, 100.0);

        when(customerService.getCustomerId(customerUserName)).thenReturn(customerId);
        when(assetRepository.findByCustomerIdAndName(customerId, TRY_ASSET_NAME)).thenReturn(Optional.of(tryAsset));

        assetService.withdrawMoney(customerUserName, amount, iban);

        assertEquals(50.0, tryAsset.getSize());
        assertEquals(50.0, tryAsset.getUsableSize());
        verify(assetRepository, times(1)).save(tryAsset);
    }

    @Test
    void withdrawMoney_ShouldThrowException_WhenInsufficientUsableSize() {
        String customerUserName = "testUser";
        Double amount = 150.0;
        String iban = "TR000000000000000000000000";
        long customerId = 1L;
        Asset tryAsset = new Asset(customerId, TRY_ASSET_NAME, 100.0, 100.0);

        when(customerService.getCustomerId(customerUserName)).thenReturn(customerId);
        when(assetRepository.findByCustomerIdAndName(customerId, TRY_ASSET_NAME)).thenReturn(Optional.of(tryAsset));

        assertThrows(AssetUsableSizeNotEnoughException.class, () -> assetService.withdrawMoney(customerUserName, amount, iban));
    }

    @Test
    void listAssets_ShouldReturnAssetDTOList_WhenAssetsExist() {
        String customerUserName = "testUser";
        String assetName = "AAPL";
        long customerId = 1L;
        Asset asset = new Asset(customerId, assetName, 100.0, 100.0);

        when(customerService.getCustomerId(customerUserName)).thenReturn(customerId);
        when(assetRepository.findAssets(customerId, assetName)).thenReturn(List.of(asset));

        List<AssetDTO> assets = assetService.listAssets(customerUserName, assetName);

        assertEquals(1, assets.size());
        assertEquals(asset.getName(), assets.get(0).name);
        verify(assetRepository, times(1)).findAssets(customerId, assetName);
    }
}
