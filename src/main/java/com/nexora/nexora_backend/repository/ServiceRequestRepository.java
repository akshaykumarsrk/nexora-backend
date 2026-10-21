package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findAllByOrganizationId(
            Long organizationId
    );

    Optional<ServiceRequest> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<ServiceRequest> findAllByCustomerIdAndOrganizationId(
            Long customerId,
            Long organizationId
    );

    List<ServiceRequest> findAllBySiteIdAndOrganizationId(
            Long siteId,
            Long organizationId
    );

    List<ServiceRequest> findAllByAssetIdAndOrganizationId(
            Long assetId,
            Long organizationId
    );

    long countByOrganizationIdAndStatus(
            Long organizationId,
            String status
    );
}