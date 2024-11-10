package com.seckin.stockmanager.repository;

import com.seckin.stockmanager.model.Order;
import com.seckin.stockmanager.model.OrderSide;
import com.seckin.stockmanager.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    Optional<Order> findByIdAndStatus(long id, OrderStatus status);
    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId "
            + "AND (:minDate IS NULL OR o.createDate >= :minDate) "
            + "AND (:maxDate IS NULL OR o.createDate <= :maxDate) "
            + "AND (:orderSide IS NULL OR o.orderSide = :orderSide) "
            + "AND (:assetName IS NULL OR o.assetName = :assetName)")
    List<Order> findOrders(
            @Param("customerId") Long customerId,
            @Param("minDate") Instant minDate,
            @Param("maxDate") Instant maxDate,
            @Param("orderSide") OrderSide orderSide,
            @Param("assetName") String assetName
    );
}

