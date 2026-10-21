package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.LoginRequest;
import com.nexora.nexora_backend.dto.RegisterRequest;
import com.nexora.nexora_backend.dto.RegisterResponse;
import com.nexora.nexora_backend.entity.User;
import com.nexora.nexora_backend.repository.UserRepository;
import com.nexora.nexora_backend.service.AuthService;
import com.nexora.nexora_backend.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request) {

        final Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException exception) {
            // Do not reveal whether the username exists or the account is disabled.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials or account unavailable");
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Authenticated user not found"));

        if (!user.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Please verify your email before logging in");
        }

        if (user.getOrganization() == null) {
            throw new IllegalStateException(
                    "User has no organization. Contact your administrator.");
        }

        // Use current database values, never role/org supplied by the client.
        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole(),
                user.getOrganization().getId()
        );

        return ResponseEntity.ok(token);
    }
}
