
package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.InvoiceConverter;
import com.nexora.nexora_backend.dto.invoice.InvoiceRequest;
import com.nexora.nexora_backend.dto.invoice.InvoiceResponse;
import com.nexora.nexora_backend.entity.Invoice;
import com.nexora.nexora_backend.entity.Job;
import com.nexora.nexora_backend.entity.JobPart;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.InvoiceRepository;
import com.nexora.nexora_backend.repository.JobPartRepository;
import com.nexora.nexora_backend.repository.JobRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode CURRENCY_ROUNDING =
            RoundingMode.HALF_UP;

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

    private static final BigDecimal ONE_HUNDRED =
            new BigDecimal("100");

    private final InvoiceRepository invoiceRepository;
    private final JobRepository jobRepository;
    private final JobPartRepository jobPartRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {

        Long organizationId = organizationContext.getOrganizationId();

        if (request == null) {
            throw new IllegalArgumentException(
                    "Invoice request cannot be null"
            );
        }

        if (request.getJobId() == null) {
            throw new IllegalArgumentException("Job ID is required");
        }

        Job job = jobRepository
                .findByIdAndOrganizationIdForUpdate(
                        request.getJobId(),
                        organizationId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Job not found")
                );

        if (!"COMPLETED".equals(job.getStatus())) {
            throw new IllegalArgumentException(
                    "Invoice can only be created for a completed job"
            );
        }

        if (invoiceRepository.existsByJobIdAndOrganizationId(
                job.getId(),
                organizationId
        )) {
            throw new IllegalArgumentException(
                    "Invoice already exists for this job"
            );
        }

        List<JobPart> jobParts =
                jobPartRepository
                        .findAllByJobIdAndOrganizationIdOrderByCreatedAtDesc(
                                job.getId(),
                                organizationId
                        );

        BigDecimal partsAmount = jobParts.stream()
                .map(jobPart -> {
                    BigDecimal price = jobPart.getTotalPrice();

                    if (price == null) {
                        return ZERO;
                    }

                    if (price.signum() < 0) {
                        throw new IllegalArgumentException(
                                "Job part total price cannot be negative"
                        );
                    }

                    return price.setScale(
                            CURRENCY_SCALE,
                            CURRENCY_ROUNDING
                    );
                })
                .reduce(ZERO, BigDecimal::add)
                .setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

        BigDecimal labourAmount = moneyOrZero(
                request.getLabourAmount()
        );

        BigDecimal otherCharges = moneyOrZero(
                request.getOtherCharges()
        );

        BigDecimal discount = moneyOrZero(
                request.getDiscount()
        );

        BigDecimal taxPercentage = request.getTaxPercentage() != null
                ? request.getTaxPercentage()
                : BigDecimal.ZERO;

        if (labourAmount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Labour amount cannot be negative"
            );
        }

        if (otherCharges.signum() < 0) {
            throw new IllegalArgumentException(
                    "Other charges cannot be negative"
            );
        }

        if (discount.signum() < 0) {
            throw new IllegalArgumentException(
                    "Discount cannot be negative"
            );
        }

        if (taxPercentage.signum() < 0) {
            throw new IllegalArgumentException(
                    "Tax percentage cannot be negative"
            );
        }

        if (taxPercentage.compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException(
                    "Tax percentage cannot be greater than 100"
            );
        }

        BigDecimal subtotal = labourAmount
                .add(partsAmount)
                .add(otherCharges)
                .subtract(discount)
                .setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

        if (subtotal.signum() < 0) {
            throw new IllegalArgumentException(
                    "Discount cannot be greater than the subtotal"
            );
        }

        BigDecimal taxAmount = subtotal
                .multiply(taxPercentage)
                .divide(
                        ONE_HUNDRED,
                        CURRENCY_SCALE,
                        CURRENCY_ROUNDING
                );

        BigDecimal totalAmount = subtotal
                .add(taxAmount)
                .setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

        LocalDate dueDate = request.getDueDate();

        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Due date cannot be in the past"
            );
        }

        String invoiceNumber = "INV-" + System.currentTimeMillis() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Organization organization = entityManager.getReference(
                Organization.class,
                organizationId
        );

        Invoice invoice = Invoice.builder()
                .organization(organization)
                .job(job)
                .invoiceNumber(invoiceNumber)
                .labourAmount(labourAmount)
                .partsAmount(partsAmount)
                .otherCharges(otherCharges)
                .discount(discount)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .status("ISSUED")
                .paymentStatus("UNPAID")
                .issuedAt(LocalDate.now())
                .dueDate(dueDate)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return InvoiceConverter.invoiceToInvoiceResponse(savedInvoice);
    }

    private BigDecimal moneyOrZero(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }

        return value.setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);
    }

    public List<InvoiceResponse> getAllInvoices() {
        Long organizationId = organizationContext.getOrganizationId();

        return invoiceRepository
                .findAllByOrganizationIdOrderByCreatedAtDesc(
                        organizationId
                )
                .stream()
                .map(InvoiceConverter::invoiceToInvoiceResponse)
                .toList();
    }

    public InvoiceResponse getInvoiceById(Long id) {
        Long organizationId = organizationContext.getOrganizationId();

        Invoice invoice = invoiceRepository
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found")
                );

        return InvoiceConverter.invoiceToInvoiceResponse(invoice);
    }

    public InvoiceResponse getInvoiceByJob(Long jobId) {
        Long organizationId = organizationContext.getOrganizationId();

        jobRepository.findByIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Job not found")
                );

        Invoice invoice = invoiceRepository
                .findByJobIdAndOrganizationId(jobId, organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Invoice not found for this job"
                        )
                );

        return InvoiceConverter.invoiceToInvoiceResponse(invoice);
    }
}
