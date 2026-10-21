package com.nexora.nexora_backend.dto.inventory;

import com.nexora.nexora_backend.entity.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StockMovementResponse {

    private Long id;

    private Long inventoryId;

    private Long partId;

    private String partCode;

    private String partName;

    private MovementType movementType;

    private Integer quantity;

    private Integer quantityBefore;

    private Integer quantityAfter;

    private String reason;

    private Long jobId;

    private Long technicianId;

    private LocalDateTime createdAt;
}