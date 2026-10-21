package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.AssetConverter;
import com.nexora.nexora_backend.dto.asset.AssetRequest;
import com.nexora.nexora_backend.dto.asset.AssetResponse;
import com.nexora.nexora_backend.entity.Asset;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Site;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.AssetRepository;
import com.nexora.nexora_backend.repository.SiteRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final SiteRepository siteRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    public AssetService(
            AssetRepository assetRepository,
            SiteRepository siteRepository,
            OrganizationContext organizationContext,
            EntityManager entityManager
    ) {
        this.assetRepository = assetRepository;
        this.siteRepository = siteRepository;
        this.organizationContext = organizationContext;
        this.entityManager = entityManager;
    }

    public List<AssetResponse> getAssets() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return assetRepository
                .findAllByOrganizationId(organizationId)
                .stream()
                .map(AssetConverter::assetToAssetResponse)
                .toList();
    }

    public AssetResponse getAsset(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Asset asset =
                assetRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Asset not found"
                                )
                        );

        return AssetConverter.assetToAssetResponse(asset);
    }

    public AssetResponse createAsset(
            AssetRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Site site =
                siteRepository
                        .findByIdAndOrganizationId(
                                request.getSiteId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found"
                                )
                        );

        if (assetRepository.existsByAssetCodeAndOrganizationId(
                request.getAssetCode(),
                organizationId
        )) {
            throw new IllegalArgumentException(
                    "Asset code already exists"
            );
        }

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );

        Asset asset = AssetConverter.assetRequestToAsset(request, site);

        asset.setOrganization(organization);

        Asset savedAsset =
                assetRepository.save(asset);

        return AssetConverter.assetToAssetResponse(
                savedAsset
        );
    }

    public AssetResponse updateAsset(
            Long id,
            AssetRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Asset asset =
                assetRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Asset not found"
                                )
                        );

        Site site =
                siteRepository
                        .findByIdAndOrganizationId(
                                request.getSiteId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found"
                                )
                        );

        if (!asset.getAssetCode().equals(request.getAssetCode())
                && assetRepository.existsByAssetCodeAndOrganizationId(
                request.getAssetCode(),
                organizationId
        )) {

            throw new IllegalArgumentException(
                    "Asset code already exists"
            );
        }

        asset.setSite(site);

        AssetConverter.updateAssetFromRequest(
                asset,
                request
        );

        Asset updatedAsset =
                assetRepository.save(asset);

        return AssetConverter.assetToAssetResponse(
                updatedAsset
        );
    }

    public void deleteAsset(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Asset asset =
                assetRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Asset not found"
                                )
                        );

        assetRepository.delete(asset);
    }

    public List<AssetResponse> getAssetsBySite(
            Long siteId
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        siteRepository
                .findByIdAndOrganizationId(
                        siteId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Site not found"
                        )
                );

        return assetRepository
                .findAllBySiteIdAndOrganizationId(
                        siteId,
                        organizationId
                )
                .stream()
                .map(AssetConverter::assetToAssetResponse)
                .toList();
    }
}