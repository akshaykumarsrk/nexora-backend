package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.JobCardConverter;
import com.nexora.nexora_backend.dto.jobcard.JobCardRequest;
import com.nexora.nexora_backend.dto.jobcard.JobCardResponse;
import com.nexora.nexora_backend.entity.Job;
import com.nexora.nexora_backend.entity.JobCard;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Technician;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.JobCardRepository;
import com.nexora.nexora_backend.repository.JobRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobCardService {

    private final JobCardRepository jobCardRepository;
    private final JobRepository jobRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    public List<JobCardResponse> getAllJobCards() {

        Long organizationId =
                organizationContext.getOrganizationId();

        List<JobCard> cards = isTechnician()
                ? jobCardRepository.findAllByOrganizationIdAndTechnician_User_Username(organizationId, currentUsername())
                : jobCardRepository.findAllByOrganizationId(organizationId);
        return cards.stream()
                .map(JobCardConverter::jobCardToJobCardResponse)
                .toList();
    }

    public JobCardResponse getJobCardById(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        JobCard jobCard =
                jobCardRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job card not found"
                                )
                        );

        ensureTechnicianOwns(jobCard);
        return JobCardConverter
                .jobCardToJobCardResponse(jobCard);
    }

    public JobCardResponse getJobCardByJobId(
            Long jobId
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        JobCard jobCard =
                jobCardRepository
                        .findByJobIdAndOrganizationId(
                                jobId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job card not found for this job"
                                )
                        );

        ensureTechnicianOwns(jobCard);
        return JobCardConverter
                .jobCardToJobCardResponse(jobCard);
    }

    public JobCardResponse createJobCard(
            JobCardRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                request.getJobId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );

        if (isTechnician() && (job.getTechnician() == null || job.getTechnician().getUser() == null
                || !currentUsername().equals(job.getTechnician().getUser().getUsername()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You can only create a job card for a job assigned to you");
        }

        if (job.getTechnician() == null) {

            throw new IllegalArgumentException(
                    "A technician must be assigned to the job before creating a job card"
            );
        }

        if ("CANCELLED".equals(job.getStatus())) {

            throw new IllegalArgumentException(
                    "Cannot create a job card for a cancelled job"
            );
        }

        if (jobCardRepository
                .findByJobIdAndOrganizationId(
                        request.getJobId(),
                        organizationId
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "A job card already exists for this job"
            );
        }

        Technician technician =
                job.getTechnician();

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );

        JobCard jobCard =
                JobCardConverter
                        .jobCardRequestToJobCard(
                                request,
                                job,
                                technician
                        );

        jobCard.setOrganization(organization);

        JobCard savedJobCard =
                jobCardRepository.save(jobCard);

        return JobCardConverter
                .jobCardToJobCardResponse(savedJobCard);
    }

    public JobCardResponse updateJobCard(
            Long id,
            JobCardRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        JobCard jobCard =
                jobCardRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job card not found"
                                )
                        );

        ensureTechnicianOwns(jobCard);

        if (Boolean.TRUE.equals(
                jobCard.getCustomerAcknowledged()
        )) {

            throw new IllegalArgumentException(
                    "A customer-acknowledged job card cannot be modified"
            );
        }

        JobCardConverter.updateJobCardFromRequest(
                jobCard,
                request
        );

        JobCard updatedJobCard =
                jobCardRepository.save(jobCard);

        return JobCardConverter
                .jobCardToJobCardResponse(updatedJobCard);
    }

    public JobCardResponse acknowledgeJobCard(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        JobCard jobCard =
                jobCardRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job card not found"
                                )
                        );

        ensureTechnicianOwns(jobCard);

        if (Boolean.TRUE.equals(
                jobCard.getCustomerAcknowledged()
        )) {

            throw new IllegalArgumentException(
                    "Job card is already acknowledged"
            );
        }

        jobCard.setCustomerAcknowledged(true);
        jobCard.setAcknowledgedAt(
                LocalDateTime.now()
        );

        JobCard savedJobCard =
                jobCardRepository.save(jobCard);

        return JobCardConverter
                .jobCardToJobCardResponse(savedJobCard);
    }

    public void deleteJobCard(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        JobCard jobCard =
                jobCardRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job card not found"
                                )
                        );

        jobCardRepository.delete(jobCard);
    }

    private boolean isTechnician() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_TECHNICIAN".equals(a.getAuthority()));
    }

    private String currentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "" : authentication.getName();
    }

    private void ensureTechnicianOwns(JobCard card) {
        if (!isTechnician()) return;
        if (card.getTechnician() == null || card.getTechnician().getUser() == null
                || !currentUsername().equals(card.getTechnician().getUser().getUsername())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You can only access job cards for jobs assigned to you");
        }
    }
}