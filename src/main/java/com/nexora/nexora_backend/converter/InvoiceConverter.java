package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.invoice.InvoiceResponse;
import com.nexora.nexora_backend.entity.Invoice;

public class InvoiceConverter {

    public static InvoiceResponse invoiceToInvoiceResponse(
            Invoice invoice
    ) {

        return InvoiceResponse.builder()
                .id(invoice.getId())

                .jobId(
                        invoice.getJob().getId()
                )

                .jobNumber(
                        invoice.getJob().getJobNumber()
                )

                .customerId(
                        invoice.getJob()
                                .getCustomer()
                                .getId()
                )

                .customerName(
                        invoice.getJob()
                                .getCustomer()
                                .getName()
                )

                .invoiceNumber(
                        invoice.getInvoiceNumber()
                )

                .labourAmount(
                        invoice.getLabourAmount()
                )

                .partsAmount(
                        invoice.getPartsAmount()
                )

                .otherCharges(
                        invoice.getOtherCharges()
                )

                .discount(
                        invoice.getDiscount()
                )

                .subtotal(
                        invoice.getSubtotal()
                )

                .taxAmount(
                        invoice.getTaxAmount()
                )

                .totalAmount(
                        invoice.getTotalAmount()
                )

                .status(
                        invoice.getStatus()
                )

                .paymentStatus(
                        invoice.getPaymentStatus()
                )

                .issuedAt(
                        invoice.getIssuedAt()
                )

                .dueDate(
                        invoice.getDueDate()
                )

                .createdAt(
                        invoice.getCreatedAt()
                )

                .updatedAt(
                        invoice.getUpdatedAt()
                )

                .build();
    }
}