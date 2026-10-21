package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository
        extends JpaRepository<Asset, Long> {

    List<Asset> findAllByOrganizationId(Long organizationId);

    Optional<Asset> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<Asset> findAllBySiteIdAndOrganizationId(
            Long siteId,
            Long organizationId
    );

    boolean existsByAssetCodeAndOrganizationId(
            String assetCode,
            Long organizationId
    );

    long countByOrganizationId(Long organizationId);
}