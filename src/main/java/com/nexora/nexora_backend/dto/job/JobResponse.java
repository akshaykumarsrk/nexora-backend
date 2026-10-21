package com.nexora.nexora_backend.dto.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class JobResponse {

    private Long id;

    private String jobNumber;

    private Long serviceRequestId;

    private Long customerId;

    private Long siteId;

    private Long assetId;

    private Long technicianId;

    private String status;

    private LocalDateTime scheduledAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String workSummary;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}