package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    List<Inventory> findAllByOrganizationId(
            Long organizationId
    );

    Optional<Inventory> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    /**
     * Locks the tenant-scoped inventory row for the duration of the
     * surrounding transaction. Use for every stock-changing operation.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findWithLockByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    Optional<Inventory> findByPartIdAndOrganizationId(
            Long partId,
            Long organizationId
    );

    /** Tenant-scoped locking lookup for stock-changing workflows. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findWithLockByPartIdAndOrganizationId(
            Long partId,
            Long organizationId
    );

    boolean existsByPartIdAndOrganizationId(
            Long partId,
            Long organizationId
    );
}