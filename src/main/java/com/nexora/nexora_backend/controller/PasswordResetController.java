package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.ForgotPasswordRequest;
import com.nexora.nexora_backend.dto.ResetPasswordRequest;
import com.nexora.nexora_backend.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Same response for existing and unknown emails to prevent account enumeration.
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If an eligible account exists for that email, password reset instructions will be sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now sign in."));
    }
}
