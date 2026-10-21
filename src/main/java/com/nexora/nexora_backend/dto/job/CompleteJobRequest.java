package com.nexora.nexora_backend.dto.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteJobRequest {

    @NotBlank(message = "Work summary is required")
    @Size(max = 2000, message = "Work summary must not exceed 2000 characters")
    private String workSummary;
}