
package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.user.*;
import com.nexora.nexora_backend.service.UserInvitationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserInvitationController {

    private final UserInvitationService invitationService;

    @PostMapping("/api/users/invitations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInvitationResponse> inviteEmployee(
            @Valid @RequestBody CreateUserInvitationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.createInvitation(request));
    }

    @PostMapping("/api/auth/accept-invitation")
    public ResponseEntity<AcceptUserInvitationResponse> acceptInvitation(
            @Valid @RequestBody AcceptUserInvitationRequest request) {

        return ResponseEntity.ok(
                invitationService.acceptInvitation(request));
    }
}
