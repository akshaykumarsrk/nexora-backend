package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.job.JobRequest;
import com.nexora.nexora_backend.dto.job.JobResponse;
import com.nexora.nexora_backend.entity.Asset;
import com.nexora.nexora_backend.entity.Customer;
import com.nexora.nexora_backend.entity.Job;
import com.nexora.nexora_backend.entity.ServiceRequest;
import com.nexora.nexora_backend.entity.Site;
import com.nexora.nexora_backend.entity.Technician;

public class JobConverter {

    public static Job jobRequestToJob(
            JobRequest request,
            ServiceRequest serviceRequest,
            Customer customer,
            Site site,
            Asset asset,
            Technician technician,
            String jobNumber
    ) {

        return Job.builder()
                .serviceRequest(serviceRequest)
                .customer(customer)
                .site(site)
                .asset(asset)
                .technician(technician)
                .jobNumber(jobNumber)
                .status(technician != null ? "ASSIGNED" : "CREATED")
                .scheduledAt(request.getScheduledAt())
                .notes(request.getNotes())
                .build();
    }

    public static JobResponse jobToJobResponse(Job job) {

        return JobResponse.builder()
                .id(job.getId())
                .jobNumber(job.getJobNumber())
                .serviceRequestId(job.getServiceRequest().getId())
                .customerId(job.getCustomer().getId())
                .siteId(job.getSite().getId())
                .assetId(
                        job.getAsset() != null
                                ? job.getAsset().getId()
                                : null
                )
                .technicianId(
                        job.getTechnician() != null
                                ? job.getTechnician().getId()
                                : null
                )
                .status(job.getStatus())
                .scheduledAt(job.getScheduledAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .workSummary(job.getWorkSummary())
                .notes(job.getNotes())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    public static void updateJobFromRequest(
            Job job,
            JobRequest request,
            Customer customer,
            Site site,
            Asset asset,
            Technician technician
    ) {

        job.setCustomer(customer);
        job.setSite(site);
        job.setAsset(asset);
        job.setTechnician(technician);
        job.setScheduledAt(request.getScheduledAt());
        job.setNotes(request.getNotes());
        job.setWorkSummary(request.getWorkSummary());
    }
}