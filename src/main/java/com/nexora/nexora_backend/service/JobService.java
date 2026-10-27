package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.JobConverter;
import com.nexora.nexora_backend.dto.job.JobRequest;
import com.nexora.nexora_backend.dto.job.JobResponse;
import com.nexora.nexora_backend.dto.notification.NotificationRequest;
import com.nexora.nexora_backend.entity.*;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.*;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobPartRepository jobPartRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final AssetRepository assetRepository;
    private final TechnicianRepository technicianRepository;

    private final OrganizationContext organizationContext;

    private final EntityManager entityManager;

    private final NotificationService notificationService;

    private final EmailService emailService;

    private final InventoryRepository inventoryRepository;

    private final StockMovementService stockMovementService;


    // =========================================================
    // GET ALL JOBS
    // =========================================================

    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return jobRepository
                .findAllByOrganizationId(organizationId)
                .stream()
                .map(JobConverter::jobToJobResponse)
                .toList();
    }


    // =========================================================
    // GET JOB BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );

        // Technicians may only retrieve their own assigned jobs.
        assertTechnicianOwnsJob(job, organizationId);

        return JobConverter.jobToJobResponse(job);
    }


    // =========================================================
    // CREATE JOB
    // =========================================================

    @Transactional
    public JobResponse createJob(
            JobRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        // -----------------------------------------------------
        // 1. Find Service Request
        // -----------------------------------------------------

        ServiceRequest serviceRequest =
                serviceRequestRepository
                        .findByIdAndOrganizationId(
                                request.getServiceRequestId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service request not found"
                                )
                        );


        // -----------------------------------------------------
        // 2. Service Request must be OPEN
        // -----------------------------------------------------

        if (!"OPEN".equals(
                serviceRequest.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Job can only be created for an OPEN service request"
            );
        }


        // -----------------------------------------------------
        // 3. Find Customer
        // -----------------------------------------------------

        Customer customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                request.getCustomerId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"
                                )
                        );


        // -----------------------------------------------------
        // 4. Find Site
        // -----------------------------------------------------

        Site site =
                siteRepository
                        .findByIdAndOrganizationId(
                                request.getSiteId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found"
                                )
                        );


        // -----------------------------------------------------
        // 5. Validate Service Request relationships
        // -----------------------------------------------------

        validateServiceRequestRelationships(
                serviceRequest,
                customer,
                site
        );


        // -----------------------------------------------------
        // 6. Find Asset if provided
        // -----------------------------------------------------

        Asset asset = null;

        if (request.getAssetId() != null) {

            asset =
                    assetRepository
                            .findByIdAndOrganizationId(
                                    request.getAssetId(),
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Asset not found"
                                    )
                            );


            if (!asset.getSite().getId()
                    .equals(site.getId())) {

                throw new IllegalArgumentException(
                        "Asset does not belong to the selected site"
                );
            }
        }


        // -----------------------------------------------------
        // 7. Find Technician if provided
        // -----------------------------------------------------

        Technician technician = null;

        if (request.getTechnicianId() != null) {

            technician =
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


            if (!"ACTIVE".equals(
                    technician.getStatus()
            )) {

                throw new IllegalArgumentException(
                        "Only active technicians can be assigned"
                );
            }
        }


        // -----------------------------------------------------
        // 8. Prevent duplicate Job
        // -----------------------------------------------------

        if (jobRepository
                .findByServiceRequestIdAndOrganizationId(
                        request.getServiceRequestId(),
                        organizationId
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "A job already exists for this service request"
            );
        }


        // -----------------------------------------------------
        // 9. Organization reference
        // -----------------------------------------------------

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );


        // -----------------------------------------------------
        // 10. Generate Job Number
        // -----------------------------------------------------

        String jobNumber =
                generateJobNumber();


        // -----------------------------------------------------
        // 11. Create Job
        // -----------------------------------------------------

        Job job =
                JobConverter.jobRequestToJob(
                        request,
                        serviceRequest,
                        customer,
                        site,
                        asset,
                        technician,
                        jobNumber
                );

        job.setOrganization(organization);


        // -----------------------------------------------------
        // 12. Set initial Job status
        // -----------------------------------------------------

        if (technician != null) {

            job.setStatus("ASSIGNED");

            serviceRequest.setStatus("ASSIGNED");

        } else {

            job.setStatus("CREATED");

            serviceRequest.setStatus("OPEN");
        }


        // -----------------------------------------------------
        // 13. Save Job
        // -----------------------------------------------------

        Job savedJob =
                jobRepository.save(job);

        serviceRequestRepository.save(
                serviceRequest
        );


        // -----------------------------------------------------
        // 14. Create notification
        // -----------------------------------------------------

        notificationService.createJobNotification(
                "Job Created",
                "A new job " +
                        savedJob.getJobNumber() +
                        " has been created.",
                "JOB",
                savedJob.getId()
        );


        // -----------------------------------------------------
        // 15. Return response
        // -----------------------------------------------------

        return JobConverter.jobToJobResponse(
                savedJob
        );
    }


    // =========================================================
    // UPDATE JOB
    // =========================================================

    @Transactional
    public JobResponse updateJob(
            Long id,
            JobRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        // -----------------------------------------------------
        // 1. Find Job
        // -----------------------------------------------------

        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );


        // -----------------------------------------------------
        // 2. Completed / Cancelled / In-progress jobs
        //    cannot be updated
        // -----------------------------------------------------

        if ("COMPLETED".equals(job.getStatus())
                || "CANCELLED".equals(job.getStatus())
                || "IN_PROGRESS".equals(job.getStatus())
                || "ON_HOLD".equals(job.getStatus())) {

            throw new IllegalArgumentException(
                    "Completed, cancelled, in-progress or on-hold jobs cannot be updated"
            );
        }


        // -----------------------------------------------------
        // 3. Find Service Request
        // -----------------------------------------------------

        ServiceRequest serviceRequest =
                serviceRequestRepository
                        .findByIdAndOrganizationId(
                                request.getServiceRequestId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service request not found"
                                )
                        );


        // -----------------------------------------------------
        // 4. Prevent assigning another existing job
        //    to the same service request
        // -----------------------------------------------------

        if (!job.getServiceRequest()
                .getId()
                .equals(request.getServiceRequestId())) {

            if (jobRepository
                    .findByServiceRequestIdAndOrganizationId(
                            request.getServiceRequestId(),
                            organizationId
                    )
                    .isPresent()) {

                throw new IllegalArgumentException(
                        "A job already exists for this service request"
                );
            }

            // New service request must be OPEN
            if (!"OPEN".equals(
                    serviceRequest.getStatus()
            )) {

                throw new IllegalArgumentException(
                        "Job can only be assigned to an OPEN service request"
                );
            }
        }


        // -----------------------------------------------------
        // 5. Find Customer
        // -----------------------------------------------------

        Customer customer =
                customerRepository
                        .findByIdAndOrganizationId(
                                request.getCustomerId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found"
                                )
                        );


        // -----------------------------------------------------
        // 6. Find Site
        // -----------------------------------------------------

        Site site =
                siteRepository
                        .findByIdAndOrganizationId(
                                request.getSiteId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found"
                                )
                        );


        // -----------------------------------------------------
        // 7. Validate Service Request relationships
        // -----------------------------------------------------

        validateServiceRequestRelationships(
                serviceRequest,
                customer,
                site
        );


        // -----------------------------------------------------
        // 8. Find Asset if provided
        // -----------------------------------------------------

        Asset asset = null;

        if (request.getAssetId() != null) {

            asset =
                    assetRepository
                            .findByIdAndOrganizationId(
                                    request.getAssetId(),
                                    organizationId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Asset not found"
                                    )
                            );


            if (!asset.getSite().getId()
                    .equals(site.getId())) {

                throw new IllegalArgumentException(
                        "Asset does not belong to the selected site"
                );
            }
        }


        // -----------------------------------------------------
        // 9. Find Technician if provided
        // -----------------------------------------------------

        Technician technician = null;

        if (request.getTechnicianId() != null) {

            technician =
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


            if (!"ACTIVE".equals(
                    technician.getStatus()
            )) {

                throw new IllegalArgumentException(
                        "Only active technicians can be assigned"
                );
            }
        }


        // -----------------------------------------------------
        // 10. Keep reference to old Service Request
        // -----------------------------------------------------

        ServiceRequest oldServiceRequest =
                job.getServiceRequest();


        // -----------------------------------------------------
        // 11. Update Job
        // -----------------------------------------------------

        JobConverter.updateJobFromRequest(
                job,
                request,
                customer,
                site,
                asset,
                technician
        );


        // -----------------------------------------------------
        // 12. Update Service Request reference
        // -----------------------------------------------------

        job.setServiceRequest(serviceRequest);


        // -----------------------------------------------------
        // 13. Synchronize status
        // -----------------------------------------------------

//        if (technician != null) {
//
//            job.setStatus("ASSIGNED");
//
//            serviceRequest.setStatus("ASSIGNED");
//
//        } else {
//
//            job.setStatus("CREATED");
//
//            serviceRequest.setStatus("OPEN");
//        }


        // -----------------------------------------------------
        // 14. If Service Request was changed,
        //     reopen the old Service Request
        // -----------------------------------------------------

        if (oldServiceRequest != null
                && !oldServiceRequest
                .getId()
                .equals(serviceRequest.getId())) {

            oldServiceRequest.setStatus("OPEN");

            serviceRequestRepository.save(
                    oldServiceRequest
            );
        }


        // -----------------------------------------------------
        // 15. Save Job
        // -----------------------------------------------------

        Job updatedJob =
                jobRepository.save(job);


        // -----------------------------------------------------
        // 16. Save Service Request
        // -----------------------------------------------------

        serviceRequestRepository.save(
                serviceRequest
        );


        // -----------------------------------------------------
        // 17. Return response
        // -----------------------------------------------------

        return JobConverter.jobToJobResponse(
                updatedJob
        );
    }


    // =========================================================
    // ASSIGN TECHNICIAN
    // =========================================================

    @Transactional
    public JobResponse assignTechnician(
            Long id,
            Long technicianId
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );


        // -----------------------------------------------------
        // Job must be CREATED
        // -----------------------------------------------------

        if (!"CREATED".equals(
                job.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Technician can only be assigned to a CREATED job"
            );
        }


        // -----------------------------------------------------
        // Find Technician
        // -----------------------------------------------------

        Technician technician =
                technicianRepository
                        .findByIdAndOrganizationId(
                                technicianId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Technician not found"
                                )
                        );


        // -----------------------------------------------------
        // Technician must be active
        // -----------------------------------------------------

        if (!"ACTIVE".equals(
                technician.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Only active technicians can be assigned"
            );
        }


        // -----------------------------------------------------
        // Assign technician
        // -----------------------------------------------------

        job.setTechnician(technician);
        job.setStatus("ASSIGNED");


        // -----------------------------------------------------
        // Update Service Request
        // -----------------------------------------------------

        ServiceRequest serviceRequest =
                job.getServiceRequest();

        serviceRequest.setStatus("ASSIGNED");


        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        Job savedJob =
                jobRepository.save(job);

        serviceRequestRepository.save(
                serviceRequest
        );


        // -----------------------------------------------------
        // Create notification
        // -----------------------------------------------------

        notificationService.createJobNotification(
                "New Job Assigned",
                "Job " + job.getJobNumber()
                        + " has been assigned to technician "
                        + technician.getName() + ".",
                "JOB_ASSIGNED",
                job.getId()
        );

        // Email is a secondary alert; a mail-provider outage must not undo a valid assignment.
        if (technician.getEmail() != null && !technician.getEmail().isBlank()) {
            try {
                emailService.sendTechnicianJobAssignmentEmail(
                        technician.getEmail(), job.getJobNumber(),
                        job.getServiceRequest() == null ? "Service job" : job.getServiceRequest().getTitle(),
                        job.getScheduledAt() == null ? "Not scheduled yet" : job.getScheduledAt().toString());
            } catch (RuntimeException mailError) {
                org.slf4j.LoggerFactory.getLogger(JobService.class).warn(
                        "Job {} was assigned but technician email could not be sent", job.getJobNumber(), mailError);
            }
        }

        return JobConverter.jobToJobResponse(
                savedJob
        );
    }


    // =========================================================
    // SCHEDULE JOB
    // =========================================================

    @Transactional
    public JobResponse scheduleJob(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );


        // -----------------------------------------------------
        // Job must be ASSIGNED
        // -----------------------------------------------------

        if (!"ASSIGNED".equals(
                job.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Only ASSIGNED jobs can be scheduled"
            );
        }


        // -----------------------------------------------------
        // Technician required
        // -----------------------------------------------------

        if (job.getTechnician() == null) {

            throw new IllegalArgumentException(
                    "Job must have a technician before scheduling"
            );
        }


        // -----------------------------------------------------
        // Work summary required before scheduling
        // -----------------------------------------------------

        if (job.getWorkSummary() == null || job.getWorkSummary().isBlank()) {
            throw new IllegalArgumentException(
                    "Add a work summary or service instructions before scheduling the job"
            );
        }

        // -----------------------------------------------------
        // Scheduled time required
        // -----------------------------------------------------

        if (job.getScheduledAt() == null) {

            throw new IllegalArgumentException(
                    "Scheduled time is required"
            );
        }


        // -----------------------------------------------------
        // Update status
        // -----------------------------------------------------

        job.setStatus("SCHEDULED");


        Job savedJob =
                jobRepository.save(job);


        // -----------------------------------------------------
        // Create notification
        // -----------------------------------------------------

        createJobNotification(
                "Job Scheduled",
                "Job " + savedJob.getJobNumber()
                        + " has been scheduled.",
                "JOB",
                savedJob.getId()
        );


        return JobConverter.jobToJobResponse(
                savedJob
        );
    }


    // =========================================================
    // START JOB
    // =========================================================

    @Transactional
    public JobResponse startJob(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );

        assertTechnicianOwnsJob(job, organizationId);

        // -----------------------------------------------------
        // Job must be SCHEDULED
        // -----------------------------------------------------

        if (!"SCHEDULED".equals(
                job.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Only SCHEDULED jobs can be started"
            );
        }


        // -----------------------------------------------------
        // Technician required
        // -----------------------------------------------------

        if (job.getTechnician() == null) {

            throw new IllegalArgumentException(
                    "Job must have a technician before starting"
            );
        }


        // -----------------------------------------------------
        // Update Job
        // -----------------------------------------------------

        job.setStatus("IN_PROGRESS");

        job.setStartedAt(
                LocalDateTime.now()
        );


        // -----------------------------------------------------
        // Update Service Request
        // -----------------------------------------------------

        ServiceRequest serviceRequest =
                job.getServiceRequest();

        serviceRequest.setStatus("IN_PROGRESS");


        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        Job savedJob =
                jobRepository.save(job);

        serviceRequestRepository.save(
                serviceRequest
        );


        // -----------------------------------------------------
        // Create notification
        // -----------------------------------------------------

        createJobNotification(
                "Job Started",
                "Job " + savedJob.getJobNumber()
                        + " is now in progress.",
                "JOB",
                savedJob.getId()
        );


        return JobConverter.jobToJobResponse(
                savedJob
        );
    }


    // =========================================================
    // HOLD JOB
    // =========================================================

    @Transactional
    public JobResponse holdJob(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );

        assertTechnicianOwnsJob(job, organizationId);

        if (!"IN_PROGRESS".equals(
                job.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Only IN_PROGRESS jobs can be put on hold"
            );
        }


        job.setStatus("ON_HOLD");


        Job savedJob =
                jobRepository.save(job);


        // -----------------------------------------------------
        // Create notification
        // -----------------------------------------------------

        createJobNotification(
                "Job On Hold",
                "Job " + savedJob.getJobNumber()
                        + " has been put on hold.",
                "WARNING",
                savedJob.getId()
        );


        // Service Request remains IN_PROGRESS.

        return JobConverter.jobToJobResponse(
                savedJob
        );
    }


    // =========================================================
    // RESUME JOB
    // =========================================================

    @Transactional
    public JobResponse resumeJob(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );

        assertTechnicianOwnsJob(job, organizationId);

        if (!"ON_HOLD".equals(
                job.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Only ON_HOLD jobs can be resumed"
            );
        }


        job.setStatus("IN_PROGRESS");


        Job savedJob =
                jobRepository.save(job);


        // -----------------------------------------------------
        // Create notification
        // -----------------------------------------------------

        createJobNotification(
                "Job Resumed",
                "Job " + savedJob.getJobNumber()
                        + " has been resumed.",
                "JOB",
                savedJob.getId()
        );


        return JobConverter.jobToJobResponse(
                savedJob
        );
    }


    // =========================================================
    // COMPLETE JOB
    // =========================================================

    @Transactional
    public JobResponse completeJob(
            Long id,
            String workSummary
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );

        assertTechnicianOwnsJob(job, organizationId);

        // -----------------------------------------------------
        // Job must be IN_PROGRESS
        // -----------------------------------------------------

        if (!"IN_PROGRESS".equals(
                job.getStatus()
        )) {

            throw new IllegalArgumentException(
                    "Only IN_PROGRESS jobs can be completed"
            );
        }


        // -----------------------------------------------------
        // Work summary required
        // -----------------------------------------------------

        if (workSummary == null
                || workSummary.isBlank()) {

            throw new IllegalArgumentException(
                    "Work summary is required to complete the job"
            );
        }


        // -----------------------------------------------------
        // Update Job
        // -----------------------------------------------------

        job.setStatus("COMPLETED");

        job.setCompletedAt(
                LocalDateTime.now()
        );

        job.setWorkSummary(
                workSummary
        );

        // -----------------------------------------------------
        // Consume used parts from inventory
        // -----------------------------------------------------

//        consumeJobPartInventory(
//                job,
//                organizationId
//        );


        // -----------------------------------------------------
        // Update Service Request
        // -----------------------------------------------------

        ServiceRequest serviceRequest =
                job.getServiceRequest();

        serviceRequest.setStatus("RESOLVED");


        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        Job savedJob =
                jobRepository.save(job);

        serviceRequestRepository.save(
                serviceRequest
        );


        // -----------------------------------------------------
        // Create notification
        // -----------------------------------------------------

        createJobNotification(
                "Job Completed",
                "Job " + savedJob.getJobNumber()
                        + " has been completed successfully.",
                "SUCCESS",
                savedJob.getId()
        );


        return JobConverter.jobToJobResponse(
                savedJob
        );
    }


    // =========================================================
    // CANCEL JOB
    // =========================================================

    @Transactional
    public JobResponse cancelJob(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );


        // -----------------------------------------------------
        // Completed / already cancelled jobs
        // cannot be cancelled
        // -----------------------------------------------------

        if ("IN_PROGRESS".equals(job.getStatus())
                || "ON_HOLD".equals(job.getStatus())
                || "COMPLETED".equals(job.getStatus())
                || "CANCELLED".equals(job.getStatus())) {

            throw new IllegalArgumentException(
                    "Only CREATED, ASSIGNED or SCHEDULED jobs can be cancelled"
            );
        }


        // -----------------------------------------------------
        // Update Job
        // -----------------------------------------------------

        job.setStatus("CANCELLED");

        // -----------------------------------------------------
        // Release reserved inventory
        // -----------------------------------------------------

        releaseJobPartInventory(
                job,
                organizationId
        );

        // -----------------------------------------------------
        // Update Service Request
        // -----------------------------------------------------

        ServiceRequest serviceRequest =
                job.getServiceRequest();

        serviceRequest.setStatus("CANCELLED");


        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        Job savedJob =
                jobRepository.save(job);

        serviceRequestRepository.save(
                serviceRequest
        );


        // -----------------------------------------------------
        // Create notification
        // -----------------------------------------------------

        notificationService.createJobNotification(
                "Job Cancelled",
                "Job " +
                        savedJob.getJobNumber() +
                        " has been cancelled.",
                "JOB",
                savedJob.getId()
        );


        return JobConverter.jobToJobResponse(
                savedJob
        );
    }


    /**
     * Technicians may progress only jobs assigned to their linked profile.
     * ADMIN/MANAGER authorization is enforced at the controller boundary.
     */
    private void assertTechnicianOwnsJob(Job job, Long organizationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean technicianRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_TECHNICIAN".equals(authority.getAuthority()));
        if (!technicianRole) {
            return;
        }

        Technician technician = technicianRepository
                .findByUserUsernameAndOrganizationId(
                        authentication.getName(), organizationId)
                .orElseThrow(() -> new AccessDeniedException(
                        "No technician profile is linked to this account"));

        if (job.getTechnician() == null
                || !technician.getId().equals(job.getTechnician().getId())) {
            throw new AccessDeniedException(
                    "Technicians can only update jobs assigned to them");
        }
    }

    // =========================================================
    // CREATE JOB NOTIFICATION
    // =========================================================

    /**
     * Creates a notification for a job event.
     *
     * Keeping this logic in one method means
     * every Job operation does not need to repeat
     * the same NotificationRequest creation code.
     */
    private void createJobNotification(
            String title,
            String message,
            String type,
            Long jobId
    ) {

        NotificationRequest request =
                new NotificationRequest();

        request.setTitle(title);
        request.setMessage(message);
        request.setType(type);
        request.setReferenceType("JOB");
        request.setReferenceId(jobId);

        notificationService.createNotification(
                request
        );
    }


    // =========================================================
    // VALIDATE SERVICE REQUEST RELATIONSHIPS
    // =========================================================

    private void validateServiceRequestRelationships(
            ServiceRequest serviceRequest,
            Customer customer,
            Site site
    ) {

        // -----------------------------------------------------
        // Customer must match Service Request customer
        // -----------------------------------------------------

        if (!serviceRequest
                .getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new IllegalArgumentException(
                    "Customer does not belong to the service request"
            );
        }


        // -----------------------------------------------------
        // Site must match Service Request site
        // -----------------------------------------------------

        if (!serviceRequest
                .getSite()
                .getId()
                .equals(site.getId())) {

            throw new IllegalArgumentException(
                    "Site does not belong to the service request"
            );
        }
    }


    // =========================================================
    // GENERATE JOB NUMBER
    // =========================================================

    private String generateJobNumber() {

        return "JOB-" +
                System.currentTimeMillis();
    }


    // =========================================================
    // DELETE JOB
    // =========================================================

    @Transactional
    public void deleteJob(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Job job =
                jobRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Job not found"
                                )
                        );


        // -----------------------------------------------------
        // Completed jobs cannot be deleted
        // -----------------------------------------------------

        String status = job.getStatus();

        if ("COMPLETED".equals(status)
                || "IN_PROGRESS".equals(status)
                || "ON_HOLD".equals(status)) {

            throw new IllegalArgumentException(
                    "Completed, in-progress or on-hold jobs cannot be deleted"
            );
        }


        ServiceRequest serviceRequest =
                job.getServiceRequest();

        // -----------------------------------------------------
        // Release reserved inventory
        // -----------------------------------------------------

        if (!"CANCELLED".equals(status)) {
            releaseJobPartInventory(
                    job,
                    organizationId
            );
        }


        // -----------------------------------------------------
        // Delete child records first
        // -----------------------------------------------------

        jobPartRepository
                .deleteAllByJobIdAndOrganizationId(
                        id,
                        organizationId
                );


        // -----------------------------------------------------
        // Reopen Service Request
        // -----------------------------------------------------

        if (serviceRequest != null) {

            serviceRequest.setStatus("OPEN");

            serviceRequestRepository.save(
                    serviceRequest
            );
        }


        // -----------------------------------------------------
        // Delete Job
        // -----------------------------------------------------

        jobRepository.delete(job);
    }

    private void releaseJobPartInventory(
            Job job,
            Long organizationId
    ) {
        List<JobPart> jobParts =
                jobPartRepository.findAllByJobIdAndOrganizationId(
                        job.getId(),
                        organizationId
                );

        for (JobPart jobPart : jobParts) {

            if (jobPart.getPart() == null
                    || jobPart.getQuantity() == null
                    || jobPart.getQuantity() <= 0) {
                continue;
            }

            Inventory inventory = inventoryRepository
                    .findByPartIdAndOrganizationId(
                            jobPart.getPart().getId(),
                            organizationId
                    )
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for part: "
                                    + jobPart.getPart().getName()
                    ));

            stockMovementService.stockInForJob(
                    inventory.getId(),
                    jobPart.getQuantity(),
                    "Part returned because job "
                            + job.getJobNumber()
                            + " was cancelled or deleted",
                    job.getId(),
                    jobPart.getTechnician() != null
                            ? jobPart.getTechnician().getId()
                            : null
            );
        }
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getMyJobs() {

        Long organizationId =
                organizationContext.getOrganizationId();

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AccessDeniedException("Authentication required");
        }

        String username = authentication.getName();

        return jobRepository
                .findAllByOrganizationIdAndTechnician_User_Username(
                        organizationId,
                        username
                )
                .stream()
                .map(JobConverter::jobToJobResponse)
                .toList();
    }
}