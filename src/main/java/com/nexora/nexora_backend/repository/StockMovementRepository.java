package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement>
    findAllByOrganizationIdOrderByCreatedAtDesc(
            Long organizationId
    );

    List<StockMovement>
    findAllByInventoryIdAndOrganizationIdOrderByCreatedAtDesc(
            Long inventoryId,
            Long organizationId
    );
}