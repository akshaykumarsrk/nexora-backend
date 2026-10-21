package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Job;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findAllByOrganizationId(Long organizationId);

    Optional<Job> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    /** Serializes invoice creation and other critical operations for one tenant-owned job. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT j
        FROM Job j
        WHERE j.id = :jobId
          AND j.organization.id = :organizationId
        """)
    Optional<Job> findByIdAndOrganizationIdForUpdate(
            @Param("jobId") Long jobId,
            @Param("organizationId") Long organizationId
    );

    Optional<Job> findByServiceRequestIdAndOrganizationId(
            Long serviceRequestId,
            Long organizationId
    );

    Optional<Job> findByServiceRequestIdAndOrganizationIdAndIdNot(
            Long serviceRequestId,
            Long organizationId,
            Long id
    );

    boolean existsByJobNumber(String jobNumber);

    long countByOrganizationIdAndStatus(
            Long organizationId,
            String status
    );

    @Query("""
        SELECT j.status, COUNT(j)
        FROM Job j
        WHERE j.organization.id = :organizationId
        GROUP BY j.status
        ORDER BY j.status
        """)
    List<Object[]> countJobsByStatus(
            @Param("organizationId") Long organizationId
    );

    List<Job> findAllByOrganizationIdAndTechnician_User_Username(
            Long organizationId,
            String username
    );
}