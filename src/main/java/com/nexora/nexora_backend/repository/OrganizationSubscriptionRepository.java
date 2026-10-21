package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.OrganizationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationSubscriptionRepository extends JpaRepository<OrganizationSubscription, Long> {
    Optional<OrganizationSubscription> findByOrganizationId(Long organizationId);
    boolean existsByOrganizationId(Long organizationId);
}
