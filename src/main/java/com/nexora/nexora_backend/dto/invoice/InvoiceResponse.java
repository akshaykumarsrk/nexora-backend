package com.nexora.nexora_backend.dto.invoice;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InvoiceResponse {

    private Long id;

    private String invoiceNumber;

    private Long jobId;

    private String jobNumber;

    private Long customerId;

    private String customerName;

    private BigDecimal labourAmount;

    private BigDecimal partsAmount;

    private BigDecimal otherCharges;

    private BigDecimal discount;

    private BigDecimal subtotal;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private String status;

    private String paymentStatus;

    private LocalDate issuedAt;

    private LocalDate dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}