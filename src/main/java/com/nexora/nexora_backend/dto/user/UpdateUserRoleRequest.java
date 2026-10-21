package com.nexora.nexora_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRoleRequest {
    @NotBlank(message = "Role is required")
    private String role;
}
