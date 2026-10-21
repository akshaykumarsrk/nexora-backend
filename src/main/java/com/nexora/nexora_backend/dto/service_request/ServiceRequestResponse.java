package com.nexora.nexora_backend.dto.service_request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ServiceRequestResponse {

    private Long id;

    private Long customerId;

    private Long siteId;

    private Long assetId;

    private String title;

    private String description;

    private String priority;

    private String status;

    private LocalDateTime requestedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}