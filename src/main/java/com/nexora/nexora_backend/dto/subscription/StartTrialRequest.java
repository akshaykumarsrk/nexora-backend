package com.nexora.nexora_backend.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StartTrialRequest(
        @NotBlank(message = "Plan code is required")
        @Pattern(regexp = "^[A-Za-z0-9_-]{2,40}$", message = "Invalid plan code")
        String planCode
) {}
