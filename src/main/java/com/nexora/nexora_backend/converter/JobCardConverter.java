package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.jobcard.JobCardRequest;
import com.nexora.nexora_backend.dto.jobcard.JobCardResponse;
import com.nexora.nexora_backend.entity.Job;
import com.nexora.nexora_backend.entity.JobCard;
import com.nexora.nexora_backend.entity.Technician;

public class JobCardConverter {

    public static JobCard jobCardRequestToJobCard(
            JobCardRequest request,
            Job job,
            Technician technician
    ) {

        return JobCard.builder()
                .job(job)
                .technician(technician)
                .problemReported(request.getProblemReported())
                .diagnosis(request.getDiagnosis())
                .workPerformed(request.getWorkPerformed())
                .findings(request.getFindings())
                .technicianRemarks(request.getTechnicianRemarks())
                .customerRemarks(request.getCustomerRemarks())
                .customerAcknowledged(false)
                .build();
    }

    public static JobCardResponse jobCardToJobCardResponse(
            JobCard jobCard
    ) {

        return JobCardResponse.builder()
                .id(jobCard.getId())
                .jobId(jobCard.getJob().getId())
                .technicianId(jobCard.getTechnician().getId())
                .problemReported(jobCard.getProblemReported())
                .diagnosis(jobCard.getDiagnosis())
                .workPerformed(jobCard.getWorkPerformed())
                .findings(jobCard.getFindings())
                .technicianRemarks(jobCard.getTechnicianRemarks())
                .customerRemarks(jobCard.getCustomerRemarks())
                .customerAcknowledged(
                        jobCard.getCustomerAcknowledged()
                )
                .acknowledgedAt(jobCard.getAcknowledgedAt())
                .createdAt(jobCard.getCreatedAt())
                .updatedAt(jobCard.getUpdatedAt())
                .build();
    }

    public static void updateJobCardFromRequest(
            JobCard jobCard,
            JobCardRequest request
    ) {

        jobCard.setProblemReported(
                request.getProblemReported()
        );

        jobCard.setDiagnosis(
                request.getDiagnosis()
        );

        jobCard.setWorkPerformed(
                request.getWorkPerformed()
        );

        jobCard.setFindings(
                request.getFindings()
        );

        jobCard.setTechnicianRemarks(
                request.getTechnicianRemarks()
        );

        jobCard.setCustomerRemarks(
                request.getCustomerRemarks()
        );
    }
}