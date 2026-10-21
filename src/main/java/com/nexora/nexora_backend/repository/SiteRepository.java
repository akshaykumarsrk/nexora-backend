package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository
        extends JpaRepository<Site, Long> {

    List<Site> findAllByOrganizationId(Long organizationId);

    Optional<Site> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<Site> findAllByCustomerIdAndOrganizationId(
            Long customerId,
            Long organizationId
    );

    long countByOrganizationId(Long organizationId);
}