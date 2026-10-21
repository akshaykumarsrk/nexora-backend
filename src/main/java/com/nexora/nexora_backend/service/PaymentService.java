
package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.PaymentConverter;
import com.nexora.nexora_backend.dto.common.PageResponse;
import com.nexora.nexora_backend.dto.payment.PaymentRequest;
import com.nexora.nexora_backend.dto.payment.PaymentResponse;
import com.nexora.nexora_backend.dto.payment.PaymentSummaryResponse;
import com.nexora.nexora_backend.entity.Invoice;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Payment;
import com.nexora.nexora_backend.exception.DuplicateTransactionReferenceException;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.InvoiceRepository;
import com.nexora.nexora_backend.repository.PaymentRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode CURRENCY_ROUNDING =
            RoundingMode.HALF_UP;

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

    private static final Set<String> ALLOWED_PAYMENT_METHODS =
            Set.of(
                    "CASH",
                    "UPI",
                    "CARD",
                    "BANK_TRANSFER",
                    "CHEQUE",
                    "OTHER"
            );

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {

        Long organizationId = organizationContext.getOrganizationId();

        if (request == null) {
            throw new IllegalArgumentException(
                    "Payment request cannot be null"
            );
        }

        if (request.getInvoiceId() == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        if (request.getAmount() == null
                || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        BigDecimal paymentAmount = request.getAmount()
                .setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

        if (paymentAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be at least 0.01"
            );
        }

        String rawPaymentMethod = request.getPaymentMethod();

        if (rawPaymentMethod == null || rawPaymentMethod.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment method is required"
            );
        }

        String paymentMethod =
                rawPaymentMethod.trim().toUpperCase(Locale.ROOT);

        if (!ALLOWED_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new IllegalArgumentException(
                    "Unsupported payment method: " + paymentMethod
            );
        }

        String transactionReference =
                normalizeTransactionReference(
                        request.getTransactionReference()
                );

        if (transactionReference != null
                && paymentRepository
                .existsByTransactionReferenceAndOrganizationId(
                        transactionReference,
                        organizationId
                )) {
            throw new DuplicateTransactionReferenceException(
                    "Transaction reference has already been used"
            );
        }

        // Lock the invoice row to prevent concurrent overpayments.
        Invoice invoice = invoiceRepository
                .findByIdAndOrganizationIdForUpdate(
                        request.getInvoiceId(),
                        organizationId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found")
                );

        // Invoice lifecycle status is separate from payment status.
        if ("VOID".equalsIgnoreCase(invoice.getStatus())
                || "CANCELLED".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException(
                    "Payment cannot be recorded for a "
                            + invoice.getStatus().toLowerCase(Locale.ROOT)
                            + " invoice"
            );
        }

        if ("DRAFT".equalsIgnoreCase(invoice.getStatus())) {
            throw new IllegalArgumentException(
                    "Payment cannot be recorded for a draft invoice"
            );
        }

        BigDecimal invoiceTotal = moneyOrZero(invoice.getTotalAmount());

        if (invoiceTotal.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Invoice total must be greater than zero"
            );
        }

        BigDecimal totalPaid = moneyOrZero(
                paymentRepository.getTotalPaidForInvoice(
                        invoice.getId(),
                        organizationId
                )
        );

        BigDecimal remainingAmount = invoiceTotal.subtract(totalPaid)
                .setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

        if (remainingAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Invoice is already fully paid"
            );
        }

        if (paymentAmount.compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment exceeds remaining amount. Remaining amount: ₹"
                            + remainingAmount.toPlainString()
            );
        }

        Organization organization = entityManager.getReference(
                Organization.class,
                organizationId
        );

        Payment payment = Payment.builder()
                .organization(organization)
                .invoice(invoice)
                .amount(paymentAmount)
                .paymentMethod(paymentMethod)
                .transactionReference(transactionReference)
                .notes(request.getNotes())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newTotalPaid = totalPaid.add(paymentAmount)
                .setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

        if (newTotalPaid.compareTo(invoiceTotal) >= 0) {
            invoice.setPaymentStatus("PAID");
        } else if (newTotalPaid.signum() > 0) {
            invoice.setPaymentStatus("PARTIALLY_PAID");
        } else {
            invoice.setPaymentStatus("UNPAID");
        }

        invoiceRepository.save(invoice);

        return PaymentConverter.paymentToPaymentResponse(savedPayment);
    }

    // =========================================================
    // GET PAGINATED AND SEARCHABLE PAYMENTS
    // =========================================================

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPayments(
            Pageable pageable,
            String searchTerm
    ) {
        Long organizationId = organizationContext.getOrganizationId();

        String normalizedSearch =
                searchTerm == null ? "" : searchTerm.trim();

        Page<Payment> paymentPage;

        if (normalizedSearch.isBlank()) {
            paymentPage =
                    paymentRepository
                            .findAllByOrganizationIdOrderByPaymentDateDesc(
                                    organizationId,
                                    pageable
                            );
        } else {
            paymentPage = paymentRepository.searchByOrganizationId(
                    organizationId,
                    normalizedSearch,
                    pageable
            );
        }

        List<PaymentResponse> content = paymentPage.getContent()
                .stream()
                .map(PaymentConverter::paymentToPaymentResponse)
                .toList();

        return PageResponse.<PaymentResponse>builder()
                .content(content)
                .page(paymentPage.getNumber())
                .size(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .first(paymentPage.isFirst())
                .last(paymentPage.isLast())
                .build();
    }

    // =========================================================
    // GET PAYMENTS FOR INVOICE
    // =========================================================

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForInvoice(Long invoiceId) {

        Long organizationId = organizationContext.getOrganizationId();

        invoiceRepository.findByIdAndOrganizationId(
                        invoiceId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found")
                );

        return paymentRepository
                .findAllByInvoiceIdAndOrganizationIdOrderByPaymentDateDesc(
                        invoiceId,
                        organizationId
                )
                .stream()
                .map(PaymentConverter::paymentToPaymentResponse)
                .toList();
    }

    // =========================================================
    // PAYMENT SUMMARY
    // =========================================================

    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentSummary(Long invoiceId) {

        Long organizationId = organizationContext.getOrganizationId();

        Invoice invoice = invoiceRepository
                .findByIdAndOrganizationId(invoiceId, organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invoice not found")
                );

        BigDecimal invoiceTotal = moneyOrZero(invoice.getTotalAmount());

        BigDecimal paidAmount = moneyOrZero(
                paymentRepository.getTotalPaidForInvoice(
                        invoiceId,
                        organizationId
                )
        );

        BigDecimal remainingAmount = invoiceTotal.subtract(paidAmount);

        if (remainingAmount.signum() < 0) {
            remainingAmount = ZERO;
        }

        remainingAmount = remainingAmount.setScale(
                CURRENCY_SCALE,
                CURRENCY_ROUNDING
        );

        String paymentStatus;

        if (paidAmount.compareTo(invoiceTotal) >= 0) {
            paymentStatus = "PAID";
        } else if (paidAmount.signum() > 0) {
            paymentStatus = "PARTIALLY_PAID";
        } else {
            paymentStatus = "UNPAID";
        }

        return PaymentSummaryResponse.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceTotal(invoiceTotal)
                .paidAmount(paidAmount)
                .remainingAmount(remainingAmount)
                .paymentStatus(paymentStatus)
                .build();
    }

    private String normalizeTransactionReference(String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }

        return reference.trim();
    }

    private BigDecimal moneyOrZero(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }

        return value.setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);
    }
}
