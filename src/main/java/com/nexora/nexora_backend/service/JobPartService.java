package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.JobPartConverter;
import com.nexora.nexora_backend.dto.job_part.JobPartRequest;
import com.nexora.nexora_backend.dto.job_part.JobPartResponse;
import com.nexora.nexora_backend.entity.Inventory;
import com.nexora.nexora_backend.entity.Job;
import com.nexora.nexora_backend.entity.JobPart;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Part;
import com.nexora.nexora_backend.entity.Technician;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.InventoryRepository;
import com.nexora.nexora_backend.repository.JobPartRepository;
import com.nexora.nexora_backend.repository.JobRepository;
import com.nexora.nexora_backend.repository.PartRepository;
import com.nexora.nexora_backend.repository.TechnicianRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPartService {

    private final JobPartRepository jobPartRepository;
    private final JobRepository jobRepository;
    private final PartRepository partRepository;
    private final TechnicianRepository technicianRepository;
    private final InventoryRepository inventoryRepository;

    private final StockMovementService stockMovementService;

    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;


    // =========================================================
    // GET ALL JOB PARTS
    // =========================================================

    public List<JobPartResponse> getAllJobParts() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return jobPartRepository
                .findAllByOrganizationIdOrderByCreatedAtDesc(
                        organizationId
                )
                .stream()
                .map(JobPartConverter::entityToResponse)
                .toList();
    }


    // =========================================================
    // GET JOB PART BY ID
    // =========================================================

    public JobPartResponse getJobPartById(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        JobPart jobPart =
                jobPartRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job part not found"
                                )
                        );

        return JobPartConverter.entityToResponse(
                jobPart
        );
    }


    // =========================================================
    // GET PARTS FOR JOB
    // =========================================================

    public List<JobPartResponse> getJobPartsByJob(
            Long jobId
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        // -----------------------------------------------------
        // Make sure job belongs to organization
        // -----------------------------------------------------

        jobRepository
                .findByIdAndOrganizationId(
                        jobId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job not found"
                        )
                );

        return jobPartRepository
                .findAllByJobIdAndOrganizationIdOrderByCreatedAtDesc(
                        jobId,
                        organizationId
                )
                .stream()
                .map(JobPartConverter::entityToResponse)
                .toList();
    }


    // =========================================================
    // ADD PART TO JOB
    // =========================================================

    @Transactional
    public JobPartResponse addPartToJob(
            JobPartRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        // -----------------------------------------------------
        // 1. Find Job
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // 2. Validate Job status
        // -----------------------------------------------------

        String status = job.getStatus();

        if (!"CREATED".equals(status)
                && !"ASSIGNED".equals(status)
                && !"SCHEDULED".equals(status)) {

            throw new IllegalArgumentException(
                    "Parts can only be added to CREATED, ASSIGNED or SCHEDULED jobs"
            );
        }


        // -----------------------------------------------------
        // 3. Find Part
        // -----------------------------------------------------

        Part part =
                partRepository
                        .findByIdAndOrganizationId(
                                request.getPartId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Part not found"
                                )
                        );


        // -----------------------------------------------------
        // 4. Part must be active
        // -----------------------------------------------------

        if (!Boolean.TRUE.equals(
                part.getActive()
        )) {

            throw new IllegalArgumentException(
                    "Inactive parts cannot be added to a job"
            );
        }


        // -----------------------------------------------------
        // 5. Validate quantity
        // -----------------------------------------------------

        if (request.getQuantity() == null
                || request.getQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }


        // -----------------------------------------------------
        // 6. Find Technician
        // -----------------------------------------------------

        Technician technician =
                technicianRepository
                        .findByIdAndOrganizationId(
                                request.getTechnicianId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Technician not found"
                                )
                        );


        // -----------------------------------------------------
        // 7. Technician must be active
        // -----------------------------------------------------

        if (!"ACTIVE".equals(
                technician.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Only active technicians can add parts"
            );
        }


        // -----------------------------------------------------
        // 8. Technician must be assigned to Job
        // -----------------------------------------------------

        if (job.getTechnician() == null
                || !job.getTechnician()
                .getId()
                .equals(technician.getId())) {

            throw new IllegalArgumentException(
                    "Technician is not assigned to this job"
            );
        }


        // -----------------------------------------------------
        // 9. Find Inventory
        // -----------------------------------------------------

        Inventory inventory =
                inventoryRepository
                        .findByPartIdAndOrganizationId(
                                part.getId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found for this part"
                                )
                        );


        // -----------------------------------------------------
        // 10. Inventory must be active
        // -----------------------------------------------------

        if (!Boolean.TRUE.equals(
                inventory.getActive()
        )) {

            throw new IllegalArgumentException(
                    "Inventory for this part is inactive"
            );
        }


        // -----------------------------------------------------
        // 11. Check available stock
        // -----------------------------------------------------

        int availableQuantity =
                inventory.getQuantity()
                        - inventory.getReservedQuantity();


        if (request.getQuantity()
                > availableQuantity) {

            throw new IllegalArgumentException(
                    "Insufficient stock. Available: "
                            + availableQuantity
            );
        }


        // -----------------------------------------------------
        // 12. Organization reference
        // -----------------------------------------------------

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );


        // -----------------------------------------------------
        // 13. Create JobPart
        // -----------------------------------------------------

        JobPart jobPart =
                JobPartConverter.requestToEntity(
                        request,
                        job,
                        part,
                        technician
                );

        jobPart.setOrganization(
                organization
        );


        // -----------------------------------------------------
        // 14. Save JobPart
        // -----------------------------------------------------

        JobPart savedJobPart =
                jobPartRepository.save(
                        jobPart
                );


        // -----------------------------------------------------
        // 15. Remove stock
        // -----------------------------------------------------
        //
        // This uses the existing StockMovementService so that:
        //
        // Inventory quantity is reduced
        // +
        // STOCK_OUT movement is recorded
        //
        // -----------------------------------------------------

        stockMovementService.stockOutForJob(
                inventory.getId(),
                request.getQuantity(),
                "Part used for job "
                        + job.getJobNumber(),
                job.getId(),
                technician.getId()
        );


        // -----------------------------------------------------
        // 16. Return response
        // -----------------------------------------------------

        return JobPartConverter.entityToResponse(
                savedJobPart
        );
    }


    // =========================================================
    // DELETE JOB PART
    // =========================================================

    @Transactional
    public void deleteJobPart(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        JobPart jobPart =
                jobPartRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job part not found"
                                )
                        );


        // -----------------------------------------------------
        // Completed jobs cannot be modified
        // -----------------------------------------------------

        Job job = jobPart.getJob();

        String status = job.getStatus();

        if (!"CREATED".equals(status)
                && !"ASSIGNED".equals(status)
                && !"SCHEDULED".equals(status)) {

            throw new IllegalArgumentException(
                    "Parts can only be removed from CREATED, ASSIGNED or SCHEDULED jobs"
            );
        }

        Inventory inventory = inventoryRepository
                .findByPartIdAndOrganizationId(
                        jobPart.getPart().getId(),
                        organizationId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for this part"
                ));

        stockMovementService.stockInForJob(
                inventory.getId(),
                jobPart.getQuantity(),
                "Part returned from job " + job.getJobNumber(),
                job.getId(),
                jobPart.getTechnician() != null
                        ? jobPart.getTechnician().getId()
                        : null
        );


        // -----------------------------------------------------
        // Delete JobPart
        // -----------------------------------------------------

        jobPartRepository.delete(
                jobPart
        );
    }

    private void assertTechnicianOwnsJob(
            Job job,
            Long organizationId
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean isTechnician = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_TECHNICIAN".equals(authority.getAuthority())
                );

        if (!isTechnician) {
            return;
        }

        Technician technician = technicianRepository
                .findByUserUsernameAndOrganizationId(
                        authentication.getName(),
                        organizationId
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "No technician profile is linked to this account"
                        )
                );

        if (job.getTechnician() == null
                || !technician.getId().equals(job.getTechnician().getId())) {
            throw new AccessDeniedException(
                    "You can only access parts for your assigned jobs"
            );
        }
    }
}