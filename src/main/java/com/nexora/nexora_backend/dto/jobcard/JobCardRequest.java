package com.nexora.nexora_backend.dto.jobcard;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobCardRequest {

    private Long jobId;

    @Size(
            max = 2000,
            message = "Problem reported must not exceed 2000 characters"
    )
    private String problemReported;

    @Size(
            max = 2000,
            message = "Diagnosis must not exceed 2000 characters"
    )
    private String diagnosis;

    @Size(
            max = 2000,
            message = "Work performed must not exceed 2000 characters"
    )
    private String workPerformed;

    @Size(
            max = 2000,
            message = "Findings must not exceed 2000 characters"
    )
    private String findings;

    @Size(
            max = 2000,
            message = "Technician remarks must not exceed 2000 characters"
    )
    private String technicianRemarks;

    @Size(
            max = 2000,
            message = "Customer remarks must not exceed 2000 characters"
    )
    private String customerRemarks;
}