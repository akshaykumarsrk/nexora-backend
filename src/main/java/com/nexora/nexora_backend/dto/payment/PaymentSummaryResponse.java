package com.nexora.nexora_backend.dto.payment;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryResponse {

    private Long invoiceId;

    private String invoiceNumber;

    private BigDecimal invoiceTotal;

    private BigDecimal paidAmount;

    private BigDecimal remainingAmount;

    private String paymentStatus;
}