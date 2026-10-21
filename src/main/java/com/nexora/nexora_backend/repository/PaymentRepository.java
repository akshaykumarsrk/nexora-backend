package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // PAGINATED PAYMENTS
    Page<Payment> findAllByOrganizationIdOrderByPaymentDateDesc(
            Long organizationId,
            Pageable pageable
    );

    // SEARCH PAYMENTS WITH DATABASE PAGINATION
    @Query(
            value = "SELECT p FROM Payment p " +
                    "JOIN p.invoice i " +
                    "WHERE p.organization.id = :organizationId " +
                    "AND (" +
                    "LOWER(COALESCE(i.invoiceNumber, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(COALESCE(p.paymentMethod, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(COALESCE(p.transactionReference, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(COALESCE(p.notes, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
                    ")",
            countQuery = "SELECT COUNT(p) FROM Payment p " +
                    "JOIN p.invoice i " +
                    "WHERE p.organization.id = :organizationId " +
                    "AND (" +
                    "LOWER(COALESCE(i.invoiceNumber, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(COALESCE(p.paymentMethod, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(COALESCE(p.transactionReference, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(COALESCE(p.notes, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))" +
                    ")"
    )
    Page<Payment> searchByOrganizationId(
            @Param("organizationId") Long organizationId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    // PAYMENTS FOR INVOICE
    List<Payment> findAllByInvoiceIdAndOrganizationIdOrderByPaymentDateDesc(
            Long invoiceId,
            Long organizationId
    );

    // TOTAL PAID FOR INVOICE
    @Query(
            "SELECT COALESCE(SUM(p.amount), 0) " +
                    "FROM Payment p " +
                    "WHERE p.invoice.id = :invoiceId " +
                    "AND p.organization.id = :organizationId"
    )
    BigDecimal getTotalPaidForInvoice(
            @Param("invoiceId") Long invoiceId,
            @Param("organizationId") Long organizationId
    );

    // PAYMENT BY ID + ORGANIZATION
    Optional<Payment> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    // DUPLICATE TRANSACTION REFERENCE VALIDATION
    boolean existsByTransactionReferenceAndOrganizationId(
            String transactionReference,
            Long organizationId
    );
}