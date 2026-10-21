package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.invoice.InvoiceRequest;
import com.nexora.nexora_backend.dto.invoice.InvoiceResponse;
import com.nexora.nexora_backend.service.InvoicePdfService;
import com.nexora.nexora_backend.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    private final InvoicePdfService invoicePdfService;


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(
            @Valid @RequestBody InvoiceRequest request
    ) {

        return ResponseEntity.ok(
                invoiceService.createInvoice(request)
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {

        return ResponseEntity.ok(
                invoiceService.getAllInvoices()
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceById(id)
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/job/{jobId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByJob(
            @PathVariable Long jobId
    ) {

        return ResponseEntity.ok(
                invoiceService.getInvoiceByJob(jobId)
        );
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @PathVariable Long id
    ) {

        // First, verify that this invoice belongs to the
        // authenticated user's organization.
        InvoiceResponse invoice =
                invoiceService.getInvoiceById(id);

        // Only generate the PDF after the scoped lookup succeeds.
        byte[] pdf =
                invoicePdfService.generateInvoicePdf(id);

        String filename = invoice.getInvoiceNumber() + ".pdf";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(filename)
                        .build()
        );

        headers.setContentLength(pdf.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
