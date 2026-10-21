package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.asset.AssetRequest;
import com.nexora.nexora_backend.dto.asset.AssetResponse;
import com.nexora.nexora_backend.entity.Asset;
import com.nexora.nexora_backend.entity.Site;

public class AssetConverter {

    public static Asset assetRequestToAsset(AssetRequest request, Site site) {

        return Asset.builder()
                .site(site)
                .assetCode(request.getAssetCode())
                .name(request.getName())
                .category(request.getCategory())
                .brand(request.getBrand())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                .installationDate(request.getInstallationDate())
                .warrantyEndDate(request.getWarrantyEndDate())
                .status(request.getStatus())
                .notes(request.getNotes())
                .build();

    }

    public static AssetResponse assetToAssetResponse(Asset asset) {

        return AssetResponse.builder()
                .id(asset.getId())
                .siteId(asset.getSite().getId())
                .assetCode(asset.getAssetCode())
                .name(asset.getName())
                .category(asset.getCategory())
                .brand(asset.getBrand())
                .model(asset.getModel())
                .serialNumber(asset.getSerialNumber())
                .installationDate(asset.getInstallationDate())
                .warrantyEndDate(asset.getWarrantyEndDate())
                .status(asset.getStatus())
                .notes(asset.getNotes())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();

    }

    public static void updateAssetFromRequest(Asset asset, AssetRequest assetRequest) {

        asset.setAssetCode(assetRequest.getAssetCode());
        asset.setName(assetRequest.getName());
        asset.setCategory(assetRequest.getCategory());
        asset.setBrand(assetRequest.getBrand());
        asset.setModel(assetRequest.getModel());
        asset.setSerialNumber(assetRequest.getSerialNumber());
        asset.setInstallationDate(assetRequest.getInstallationDate());
        asset.setWarrantyEndDate(assetRequest.getWarrantyEndDate());
        asset.setStatus(assetRequest.getStatus());
        asset.setNotes(assetRequest.getNotes());

    }

}
