package com.seckin.stockmanager.repository;

import com.seckin.stockmanager.model.Asset;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Asset> findByCustomerIdAndName(long customerId, String name);
    @Query("SELECT a FROM Asset a WHERE a.customerId = :customerId "
            + "AND (:name IS NULL OR a.name = :name)")
    List<Asset> findAssets(
            @Param("customerId") Long customerId,
            @Param("name") String name
    );
}
