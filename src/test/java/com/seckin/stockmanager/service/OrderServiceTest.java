package com.seckin.stockmanager.service;

import com.seckin.stockmanager.dto.ListOrderRequestDTO;
import com.seckin.stockmanager.dto.OrderDTO;
import com.seckin.stockmanager.exception.AssetUsableSizeNotEnoughException;
import com.seckin.stockmanager.exception.ResourceNotFoundException;
import com.seckin.stockmanager.model.*;
import com.seckin.stockmanager.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static com.seckin.stockmanager.service.Constants.TRY_ASSET_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetService assetService;

    @Mock
    private CustomerService customerService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_ShouldCreateOrder_WhenValid() {
        OrderDTO orderDto = new OrderDTO();
        orderDto.customerUserName = "testUser";
        orderDto.side = OrderSide.BUY;
        orderDto.assetName = "Asset";
        orderDto.orderSize = 10.0;
        orderDto.prize = 100.0;

        Asset sellingAsset = new Asset(1L, TRY_ASSET_NAME, 1000.0, 1000.0);
        Order order = orderDto.toOrder(1L);
        order.setId(1L);

        when(customerService.getCustomerId(orderDto.customerUserName)).thenReturn(1L);
        when(assetService.getAssetWithLock(1L, TRY_ASSET_NAME)).thenReturn(sellingAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO result = orderService.createOrder(orderDto);

        assertNotNull(result);
        assertEquals(1L, result.id);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_ShouldThrowException_WhenSellingAssetNotFound() {
        OrderDTO orderDto = new OrderDTO();
        orderDto.customerUserName = "testUser";
        orderDto.side = OrderSide.BUY;
        orderDto.assetName = "Asset";
        orderDto.orderSize=10.0;
        orderDto.prize=1.0;

        when(customerService.getCustomerId(orderDto.customerUserName)).thenReturn(1L);
        when(assetService.getAssetWithLock(1L, TRY_ASSET_NAME)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderDto));
    }

    @Test
    void createOrder_ShouldThrowException_WhenInsufficientUsableSize() {
        OrderDTO orderDto = new OrderDTO();
        orderDto.customerUserName = "testUser";
        orderDto.side = OrderSide.BUY;
        orderDto.assetName = "Asset";
        orderDto.orderSize = 10000.0;
        orderDto.prize = 1.0;
        Asset sellingAsset = new Asset(1L, TRY_ASSET_NAME, 1000.0, 1000.0);

        when(customerService.getCustomerId(orderDto.customerUserName)).thenReturn(1L);
        when(assetService.getAssetWithLock(1L, TRY_ASSET_NAME)).thenReturn(sellingAsset);

        assertThrows(AssetUsableSizeNotEnoughException.class, () -> orderService.createOrder(orderDto));
    }

    @Test
    void listOrders_ShouldReturnOrders_WhenCalledWithValidData() {
        ListOrderRequestDTO request = new ListOrderRequestDTO();
        request.customerUserName = "testUser";
        Order order = new Order();
        order.setId(1L);

        when(customerService.getCustomerId(request.customerUserName)).thenReturn(1L);
        when(orderRepository.findOrders(1L, request.minDate, request.maxDate, request.orderSide, request.assetName))
                .thenReturn(List.of(order));

        List<OrderDTO> result = orderService.listOrders(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id);
    }

    @Test
    void deleteOrder_ShouldDeleteOrder_WhenOrderExistsAndUserIsAuthenticated() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderSide(OrderSide.BUY);
        order.setSize(10.0);
        order.setPrize(10.0);
        order.setAssetName("test");
        Asset sellingAsset = new Asset(1L, "test", 1000.0, 1000.0);

        when(orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(customerService.getCustomer(order.getCustomerId())).thenReturn(new Customer("testUser", "password"));
        when(assetService.getAssetWithLock(1L, TRY_ASSET_NAME)).thenReturn(sellingAsset);
        doNothing().when(customerService).validateUserAuthenticated(anyString(), any(Authentication.class));

        orderService.deleteOrder(orderId, authentication);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(assetService, times(1)).saveAsset(sellingAsset);
    }

    @Test
    void deleteOrder_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 1L;

        when(orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(orderId, authentication));
    }

    @Test
    void matchOrder_ShouldMatchOrder_WhenOrderExistsAndAssetsSufficient() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderSide(OrderSide.BUY);
        order.setAssetName("Asset");
        order.setPrize(100.0);
        order.setSize(10.0);
        order.setStatus(OrderStatus.PENDING);

        Asset sellingAsset = new Asset(order.getCustomerId(), TRY_ASSET_NAME, 1000.0, 1000.0);
        Asset buyingAsset = new Asset(order.getCustomerId(), order.getAssetName(), 0.0, 0.0);

        when(orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(assetService.getAssetWithLock(order.getCustomerId(), TRY_ASSET_NAME)).thenReturn(sellingAsset);
        when(assetService.getAssetWithLock(order.getCustomerId(), order.getAssetName())).thenReturn(buyingAsset);
        when(assetService.saveAsset(any(Asset.class))).thenReturn(buyingAsset);

        orderService.matchOrder(orderId);

        assertEquals(OrderStatus.MATCHED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(assetService, times(1)).saveAsset(sellingAsset);
        verify(assetService, times(1)).saveAsset(buyingAsset);
    }

    @Test
    void matchOrder_ShouldThrowException_WhenOrderNotFound() {
        Long orderId = 1L;

        when(orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.matchOrder(orderId));
    }

    @Test
    void matchOrder_ShouldThrowException_WhenInsufficientAssetSize() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setOrderSide(OrderSide.SELL);
        order.setAssetName("Asset");
        order.setPrize(100.0);
        order.setSize(10.0);
        order.setStatus(OrderStatus.PENDING);

        Asset sellingAsset = new Asset(order.getCustomerId(), order.getAssetName(), 5.0, 5.0);

        when(orderRepository.findByIdAndStatus(orderId, OrderStatus.PENDING)).thenReturn(Optional.of(order));
        when(assetService.getAssetWithLock(order.getCustomerId(), order.getAssetName())).thenReturn(sellingAsset);

        assertThrows(AssetUsableSizeNotEnoughException.class, () -> orderService.matchOrder(orderId));
    }
}
