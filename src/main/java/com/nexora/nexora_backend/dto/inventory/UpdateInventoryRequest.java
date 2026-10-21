package com.nexora.nexora_backend.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInventoryRequest {

    @NotNull(message = "Reorder level is required")
    @Min(
            value = 0,
            message = "Reorder level cannot be negative"
    )
    private Integer reorderLevel;
}