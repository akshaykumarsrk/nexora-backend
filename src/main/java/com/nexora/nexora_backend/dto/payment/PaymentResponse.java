package com.nexora.nexora_backend.dto.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;

    private Long invoiceId;

    private String invoiceNumber;

    private BigDecimal amount;

    private String paymentMethod;

    private String transactionReference;

    private String notes;

    private LocalDateTime paymentDate;

    private LocalDateTime createdAt;
}