package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam("token") String token
    ) {
        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(
                "Email verified successfully. You can now log in."
        );
    }
}