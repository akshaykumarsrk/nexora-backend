package com.nexora.nexora_backend.dto.service_request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceRequestRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Site ID is required")
    private Long siteId;

    private Long assetId;

    @NotBlank(message = "Title is required")
    @Size(
            max = 200,
            message = "Title must not exceed 200 characters"
    )
    private String title;

    @Size(
            max = 2000,
            message = "Description must not exceed 2000 characters"
    )
    private String description;

    @NotBlank(message = "Priority is required")
    private String priority;
}