package com.nexora.nexora_backend.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechnicianAssistantRequest {
    @NotBlank(message = "Issue description is required")
    @Size(max = 3000, message = "Issue description must not exceed 3000 characters")
    private String issueDescription;

    @Size(max = 200, message = "Asset type must not exceed 200 characters")
    private String assetType;

    @Size(max = 200, message = "Brand/model must not exceed 200 characters")
    private String brandModel;
}
