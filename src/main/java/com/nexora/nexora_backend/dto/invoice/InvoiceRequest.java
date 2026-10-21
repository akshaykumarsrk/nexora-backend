package com.nexora.nexora_backend.dto.invoice;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {

    @NotNull
    private Long jobId;

    @PositiveOrZero
    @Digits(integer = 17, fraction = 2)
    private BigDecimal labourAmount;

    @PositiveOrZero
    @Digits(integer = 17, fraction = 2)
    private BigDecimal otherCharges;

    @PositiveOrZero
    @Digits(integer = 17, fraction = 2)
    private BigDecimal discount;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Digits(integer = 3, fraction = 4)
    private BigDecimal taxPercentage;

    private LocalDate dueDate;
}