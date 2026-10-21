package com.nexora.nexora_backend.dto.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssetRequest {

    @NotNull(message = "Site ID is required")
    private Long siteId;

    @NotBlank(message = "Asset code is required")
    @Size(max = 50, message = "Asset code must not exceed 50 characters")
    private String assetCode;

    @NotBlank(message = "Asset name is required")
    @Size(max = 150, message = "Asset name must not exceed 150 characters")
    private String name;

    private String category;

    private String brand;

    private String model;

    private String serialNumber;

    private LocalDate installationDate;

    private LocalDate warrantyEndDate;

    @NotBlank(message = "Asset status is required")
    private String status;

    @Size(max = 1000, message = "Notes must not exceed 1000 charcters")
    private String notes;

}
