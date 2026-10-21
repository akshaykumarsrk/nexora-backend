
package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.job_part.JobPartRequest;
import com.nexora.nexora_backend.dto.job_part.JobPartResponse;
import com.nexora.nexora_backend.entity.Job;
import com.nexora.nexora_backend.entity.JobPart;
import com.nexora.nexora_backend.entity.Part;
import com.nexora.nexora_backend.entity.Technician;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class JobPartConverter {

    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode CURRENCY_ROUNDING = RoundingMode.HALF_UP;

    private JobPartConverter() {
        // Utility class
    }

    public static JobPart requestToEntity(
            JobPartRequest request,
            Job job,
            Part part,
            Technician technician
    ) {
        BigDecimal unitPrice = request.getUnitPrice()
                .setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

        BigDecimal totalPrice = unitPrice
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(CURRENCY_SCALE, CURRENCY_ROUNDING);

        return JobPart.builder()
                .organization(job.getOrganization())
                .job(job)
                .part(part)
                .technician(technician)
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .notes(request.getNotes())
                .build();
    }

    public static JobPartResponse entityToResponse(JobPart jobPart) {
        return JobPartResponse.builder()
                .id(jobPart.getId())
                .jobId(jobPart.getJob().getId())
                .partId(jobPart.getPart().getId())
                .technicianId(jobPart.getTechnician().getId())
                .quantity(jobPart.getQuantity())
                .unitPrice(jobPart.getUnitPrice())
                .totalPrice(jobPart.getTotalPrice())
                .notes(jobPart.getNotes())
                .createdAt(jobPart.getCreatedAt())
                .build();
    }
}
