
package com.nexora.nexora_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AcceptUserInvitationRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 12, max = 72)
    private String password;

    @NotBlank
    private String confirmPassword;
}
