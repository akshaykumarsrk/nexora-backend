package com.nexora.nexora_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z0-9._@-]+$", message = "Username contains unsupported characters")
    private String username;

    @NotBlank(message = "Temporary password is required")
    @Size(min = 12, max = 72, message = "Temporary password must be between 12 and 72 characters")
    private String temporaryPassword;

    @NotBlank(message = "Role is required")
    private String role;

    @Positive(message = "Technician ID must be positive")
    private Long technicianId;
}
