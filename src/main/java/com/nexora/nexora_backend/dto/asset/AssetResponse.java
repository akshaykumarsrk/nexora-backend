package com.nexora.nexora_backend.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AssetResponse {

    private Long id;

    private Long siteId;

    private String assetCode;

    private String name;

    private String category;

    private String brand;

    private String model;

    private String serialNumber;

    private LocalDate installationDate;

    private LocalDate warrantyEndDate;

    private String status;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}