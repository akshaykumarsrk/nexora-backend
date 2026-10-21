package com.nexora.nexora_backend.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Long organizationId;
    private boolean enabled;
    private boolean mustChangePassword;
}
