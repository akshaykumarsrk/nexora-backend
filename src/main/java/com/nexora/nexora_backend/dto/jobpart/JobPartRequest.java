
package com.nexora.nexora_backend.dto.job_part;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class JobPartRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotNull(message = "Part ID is required")
    private Long partId;

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Unit price cannot be negative"
    )
    @Digits(
            integer = 17,
            fraction = 2,
            message = "Unit price must have at most 17 integer digits and 2 decimal places"
    )
    private BigDecimal unitPrice;

    private String notes;
}
