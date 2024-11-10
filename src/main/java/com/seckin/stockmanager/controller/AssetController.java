package com.seckin.stockmanager.controller;

import com.seckin.stockmanager.dto.AssetDTO;
import com.seckin.stockmanager.dto.DepositRequestDTO;
import com.seckin.stockmanager.dto.ListAssetRequestDTO;
import com.seckin.stockmanager.dto.WithdrawRequestDTO;
import com.seckin.stockmanager.service.AssetService;
import com.seckin.stockmanager.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assets")
public class AssetController {

    private AssetService assetService;
    private CustomerService customerService;

    public AssetController(AssetService assetService, CustomerService customerService) {
        this.assetService = assetService;
        this.customerService=customerService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@Valid @RequestBody DepositRequestDTO request, Authentication authentication) {
        this.customerService.validateUserAuthenticated(request.customerUserName,authentication);
        assetService.depositMoney(request.customerUserName, request.amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@Valid @RequestBody WithdrawRequestDTO request,Authentication authentication){
        this.customerService.validateUserAuthenticated(request.customerUserName,authentication);
        assetService.withdrawMoney(request.customerUserName,request.amount,request.iban);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<AssetDTO>> listAssets(@Valid @RequestBody ListAssetRequestDTO request,Authentication authentication){
        this.customerService.validateUserAuthenticated(request.customerUserName,authentication);
        List<AssetDTO> result=assetService.listAssets(request.customerUserName,
                request.assetName);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
