package com.nexora.nexora_backend.dto.catalog;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class EquipmentModelResponse {
    private Long id; private Long brandId; private String brandName; private String productType;
    private String modelNumber; private String description; private Boolean active;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
