package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.JobCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobCardRepository
        extends JpaRepository<JobCard, Long> {

    List<JobCard> findAllByOrganizationId(
            Long organizationId
    );

    List<JobCard> findAllByOrganizationIdAndTechnician_User_Username(Long organizationId, String username);

    Optional<JobCard> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    Optional<JobCard> findByJobIdAndOrganizationId(
            Long jobId,
            Long organizationId
    );
}