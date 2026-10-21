package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Organization;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizationSubscriptionLockRepository extends JpaRepository<Organization, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Organization o where o.id = :id")
    Optional<Organization> findByIdForSubscriptionUpdate(@Param("id") Long id);
}
