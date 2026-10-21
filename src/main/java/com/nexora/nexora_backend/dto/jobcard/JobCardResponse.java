package com.nexora.nexora_backend.dto.jobcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class JobCardResponse {

    private Long id;

    private Long jobId;

    private Long technicianId;

    private String problemReported;

    private String diagnosis;

    private String workPerformed;

    private String findings;

    private String technicianRemarks;

    private String customerRemarks;

    private Boolean customerAcknowledged;

    private LocalDateTime acknowledgedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}