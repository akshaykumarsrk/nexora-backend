package com.nexora.nexora_backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class RevenueResponse {

    private LocalDate date;

    private BigDecimal amount;
}