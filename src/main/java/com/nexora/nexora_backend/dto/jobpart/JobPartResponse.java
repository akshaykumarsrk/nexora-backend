
package com.nexora.nexora_backend.dto.job_part;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class JobPartResponse {

    private Long id;
    private Long jobId;
    private Long partId;
    private Long technicianId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String notes;
    private LocalDateTime createdAt;
}
