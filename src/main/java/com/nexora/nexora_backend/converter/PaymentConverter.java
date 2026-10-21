package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.payment.PaymentResponse;
import com.nexora.nexora_backend.entity.Payment;

public class PaymentConverter {

    private PaymentConverter() {
    }

    public static PaymentResponse paymentToPaymentResponse(
            Payment payment
    ) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(
                        payment.getInvoice().getInvoiceNumber()
                )
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionReference(
                        payment.getTransactionReference()
                )
                .notes(payment.getNotes())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}