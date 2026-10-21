package com.nexora.nexora_backend.dto.inventory;

import com.nexora.nexora_backend.entity.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementRequest {

    @NotNull(message = "Inventory is required")
    private Long inventoryId;

    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Min(
            value = 0,
            message = "Quantity must not be negative"
    )
    private Integer quantity;

    @Size(
            max = 500,
            message = "Reason must not exceed 500 characters"
    )
    private String reason;

    private Long jobId;

    private Long technicianId;
}