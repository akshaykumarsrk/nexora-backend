package com.nexora.nexora_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank @Size(min = 32, max = 128)
    private String token;

    @NotBlank @Size(min = 8, max = 72)
    private String password;
}
