package com.nexora.nexora_backend.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InventoryResponse {

    private Long id;

    private Long partId;

    private String partCode;

    private String partName;

    private Integer quantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;

    private Integer reorderLevel;

    private Boolean lowStock;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}