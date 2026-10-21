
package com.nexora.nexora_backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserInvitationRequest {

    @NotBlank
    @Email
    @Size(max = 254)
    private String email;

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = "^[A-Za-z0-9._@-]+$",
            message = "Username contains unsupported characters"
    )
    private String username;

    @NotBlank
    @Pattern(
            regexp = "(?i)^(ADMIN|MANAGER|STAFF|TECHNICIAN)$",
            message = "Role must be ADMIN, MANAGER, STAFF or TECHNICIAN"
    )
    private String role;

    @Positive
    private Long technicianId;
}
