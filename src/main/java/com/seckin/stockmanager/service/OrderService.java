package com.seckin.stockmanager.service;

import com.seckin.stockmanager.dto.ListOrderRequestDTO;
import com.seckin.stockmanager.dto.OrderDTO;
import com.seckin.stockmanager.exception.AssetUsableSizeNotEnoughException;
import com.seckin.stockmanager.exception.ResourceNotFoundException;
import com.seckin.stockmanager.model.Asset;
import com.seckin.stockmanager.model.Order;
import com.seckin.stockmanager.model.OrderSide;
import com.seckin.stockmanager.model.OrderStatus;
import com.seckin.stockmanager.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.seckin.stockmanager.service.Constants.TRY_ASSET_NAME;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final AssetService assetService;

    private final CustomerService customerService;

    public OrderService(OrderRepository orderRepository, AssetService assetService,
                        CustomerService customerService) {
        this.assetService = assetService;
        this.orderRepository = orderRepository;
        this.customerService = customerService;
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDto) {
        Long customerId = customerService.getCustomerId(orderDto.customerUserName);
        String sellingAssetName = getSellingAssetName(orderDto.side, orderDto.assetName);
        Asset sellingAsset = assetService.getAssetWithLock(customerId,
                sellingAssetName);
        if (sellingAsset == null) {
            logger.error("Selling Asset Not Found");
            throw new ResourceNotFoundException("Selling Asset Not Found");
        }
        Double totalRequiredSize = sellingAssetName.equals(TRY_ASSET_NAME) ?
                orderDto.prize * orderDto.orderSize : orderDto.orderSize;
        validateSellingAssetSize(totalRequiredSize, sellingAsset, false);
        Order createdOrder = orderRepository.save(orderDto.toOrder(customerId));
        sellingAsset.setUsableSize(sellingAsset.getUsableSize() - totalRequiredSize);

        // version increase will cause pessimistic locking exception in case of parallel
        // order request with
        // same selling asset and prevent incorrect usable size updates
        assetService.saveAsset(sellingAsset);
        orderDto.id = createdOrder.getId();
        orderDto.createdDate = createdOrder.getCreateDate();
        orderDto.status = createdOrder.getStatus();
        return orderDto;

    }

    private static void validateSellingAssetSize(Double totalRequiredSize,
                                                 Asset sellingAsset,
                                                 boolean compareSize) {
        if (sellingAsset == null || (!compareSize && sellingAsset.getUsableSize().compareTo(totalRequiredSize) < 0)
                || (compareSize && sellingAsset.getSize().compareTo(totalRequiredSize) < 0)) {
            throw new AssetUsableSizeNotEnoughException("Insufficient Asset Usable Size");
        }
    }

    private static String getSellingAssetName(OrderSide side, String assetName) {
        String sellingAssetName;
        if (side == OrderSide.BUY) {
            sellingAssetName = TRY_ASSET_NAME;
        } else if (side == OrderSide.SELL) {
            sellingAssetName = assetName;
        } else {
            logger.error("Invalid OrderSide:" + side);
            throw new IllegalArgumentException("Invalid OrderSide:" + side);
        }
        return sellingAssetName;
    }

    public List<OrderDTO> listOrders(ListOrderRequestDTO listOrderRequestDTO) {
        Long customerId =
                customerService.getCustomerId(listOrderRequestDTO.customerUserName);
        return orderRepository.findOrders(customerId,
                        listOrderRequestDTO.minDate, listOrderRequestDTO.maxDate,
                        listOrderRequestDTO.orderSide, listOrderRequestDTO.assetName)
                .stream().map(order -> new OrderDTO(order,
                        listOrderRequestDTO.customerUserName))
                .toList();
    }

    public void deleteOrder(Long orderId, Authentication authentication) {
        Order order =
                orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING).orElseThrow(() ->
                        new ResourceNotFoundException("Deletable Order with ID" + " " + orderId + " not found"));
        String customerUserName =
                customerService.getCustomer(order.getCustomerId()).getUsername();
        customerService.validateUserAuthenticated(customerUserName, authentication);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void matchOrder(Long orderId) {
        Order order =
                orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING).orElseThrow(() ->
                        new ResourceNotFoundException("Matchable Order with ID" + " " + orderId +
                                " not found"));

        String sellingAssetName = TRY_ASSET_NAME;
        String buyingAssetName = order.getAssetName();
        if (order.getOrderSide() == OrderSide.SELL) {
            sellingAssetName = order.getAssetName();
            buyingAssetName = TRY_ASSET_NAME;
        }
        Asset sellingAsset = assetService.getAssetWithLock(order.getCustomerId(),
                sellingAssetName);
        Double totalRequiredSize = sellingAssetName.equals(TRY_ASSET_NAME) ?
                order.getPrize() * order.getSize() : order.getSize();
        validateSellingAssetSize(totalRequiredSize, sellingAsset, true);
        order.setStatus(OrderStatus.MATCHED);
        Asset buyingAsset = assetService.getAssetWithLock(order.getCustomerId(),
                buyingAssetName);
        if (buyingAsset == null) {
            buyingAsset = assetService.saveAsset(new Asset(order.getCustomerId(),
                    buyingAssetName, 0d, 0d));
        }
        Double sellingSize = totalRequiredSize;
        Double buyingSize = sellingAssetName.equals(TRY_ASSET_NAME) ? order.getSize() :
                order.getPrize() * order.getSize();
        sellingAsset.setSize(sellingAsset.getSize() - sellingSize);
        buyingAsset.setUsableSize(buyingAsset.getUsableSize() + buyingSize);
        buyingAsset.setSize(buyingAsset.getSize() + buyingSize);
        assetService.saveAsset(buyingAsset);
        assetService.saveAsset(sellingAsset);
        orderRepository.save(order);
    }
}
