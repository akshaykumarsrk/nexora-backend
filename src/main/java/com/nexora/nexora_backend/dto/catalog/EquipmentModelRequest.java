package com.nexora.nexora_backend.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EquipmentModelRequest {
    @NotNull private Long brandId;
    @NotBlank @Size(max = 100) private String productType;
    @NotBlank @Size(max = 100) private String modelNumber;
    @Size(max = 500) private String description;
    private Boolean active;
}
