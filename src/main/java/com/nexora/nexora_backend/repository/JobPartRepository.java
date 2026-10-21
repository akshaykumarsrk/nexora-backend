package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.JobPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobPartRepository
        extends JpaRepository<JobPart, Long> {

    List<JobPart> findAllByOrganizationIdOrderByCreatedAtDesc(
            Long organizationId
    );

    List<JobPart> findAllByJobIdAndOrganizationIdOrderByCreatedAtDesc(
            Long jobId,
            Long organizationId
    );

    Optional<JobPart> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    List<JobPart> findAllByJobIdAndOrganizationId(
            Long jobId,
            Long organizationId
    );

    @Modifying
    @Query("""
            DELETE FROM JobPart jp
            WHERE jp.job.id = :jobId
            AND jp.organization.id = :organizationId
            """)
    void deleteAllByJobIdAndOrganizationId(
            @Param("jobId") Long jobId,
            @Param("organizationId") Long organizationId
    );
}