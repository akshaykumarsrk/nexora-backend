package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByOrganizationIdOrderByCreatedAtDesc(
            Long organizationId
    );

    Optional<Invoice> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    Optional<Invoice> findByJobIdAndOrganizationId(
            Long jobId,
            Long organizationId
    );

    boolean existsByJobIdAndOrganizationId(
            Long jobId,
            Long organizationId
    );

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("""
            SELECT COALESCE(SUM(i.totalAmount), 0)
            FROM Invoice i
            WHERE i.organization.id = :organizationId
            """)
    BigDecimal getTotalRevenue(
            @Param("organizationId") Long organizationId
    );

    @Query("""
            SELECT i.issuedAt, COALESCE(SUM(i.totalAmount), 0)
            FROM Invoice i
            WHERE i.organization.id = :organizationId
            AND i.issuedAt IS NOT NULL
            GROUP BY i.issuedAt
            ORDER BY i.issuedAt
            """)
    List<Object[]> getRevenueByDate(
            @Param("organizationId") Long organizationId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM Invoice i
            WHERE i.id = :invoiceId
            AND i.organization.id = :organizationId
            """)
    Optional<Invoice> findByIdAndOrganizationIdForUpdate(
            @Param("invoiceId") Long invoiceId,
            @Param("organizationId") Long organizationId
    );
}