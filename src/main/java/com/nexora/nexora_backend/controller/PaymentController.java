package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.common.PageResponse;
import com.nexora.nexora_backend.dto.payment.PaymentRequest;
import com.nexora.nexora_backend.dto.payment.PaymentResponse;
import com.nexora.nexora_backend.dto.payment.PaymentSummaryResponse;
import com.nexora.nexora_backend.service.PaymentReceiptService;
import com.nexora.nexora_backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private final PaymentReceiptService paymentReceiptService;

    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(
                paymentService.createPayment(request)
        );
    }


    // =========================================================
    // PAGINATED AND SEARCHABLE PAYMENTS
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/page")
    public ResponseEntity<PageResponse<PaymentResponse>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search
    ) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be zero or greater"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "paymentDate"
                        )
                );

        return ResponseEntity.ok(
                paymentService.getPayments(
                        pageable,
                        search
                )
        );
    }

    // =========================================================
    // PAYMENTS FOR INVOICE
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsForInvoice(
            @PathVariable Long invoiceId
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentsForInvoice(
                invoiceId
        )
        );
    }

    // =========================================================
    // PAYMENT SUMMARY
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/invoice/{invoiceId}/summary")
    public ResponseEntity<PaymentSummaryResponse> getPaymentSummary(
            @PathVariable Long invoiceId
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentSummary(
                invoiceId
        )
        );
    }

    // =========================================================
    // PAYMENT RECEIPT PDF
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long paymentId
    ) {

        byte[] pdf =
                paymentReceiptService.generateReceipt(
                        paymentId
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=receipt-"
                                + paymentId
                                + ".pdf"
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .body(pdf);
    }
}
