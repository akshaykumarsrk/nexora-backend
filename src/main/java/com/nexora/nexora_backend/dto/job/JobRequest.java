package com.nexora.nexora_backend.dto.job;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class JobRequest {

    @NotNull(message = "Service request is required")
    private Long serviceRequestId;

    @NotNull(message = "Customer is required")
    private Long customerId;

    @NotNull(message = "Site is required")
    private Long siteId;

    private Long assetId;

    private Long technicianId;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Size(
            max = 2000,
            message = "Work summary must not exceed 2000 characters"
    )
    private String workSummary;

    private LocalDateTime scheduledAt;
}