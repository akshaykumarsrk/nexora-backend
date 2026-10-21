
package com.nexora.nexora_backend.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserInvitationResponse {
    private String message;
    private Instant expiresAt;
}
