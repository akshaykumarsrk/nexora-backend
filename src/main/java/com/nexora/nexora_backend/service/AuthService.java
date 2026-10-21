package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.dto.RegisterRequest;
import com.nexora.nexora_backend.dto.RegisterResponse;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.User;
import com.nexora.nexora_backend.exception.DuplicateUsernameException;
import com.nexora.nexora_backend.repository.OrganizationRepository;
import com.nexora.nexora_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String username = request.getUsername().trim();
        String organizationName = request.getOrganizationName().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String password = request.getPassword();

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Password must not exceed 72 UTF-8 bytes"
            );
        }

        if (username.isBlank() || organizationName.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Organization name, username, and email are required"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException("Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Unable to register with this email address"
            );
        }

        Organization organization = new Organization();
        organization.setName(organizationName);

        Organization savedOrganization =
                organizationRepository.save(organization);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ADMIN");
        user.setEnabled(true);
        user.setOrganization(savedOrganization);

        User savedUser = userRepository.save(user);

        emailVerificationService.createAndSendVerificationEmail(savedUser);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .organizationId(savedOrganization.getId())
                .organizationName(savedOrganization.getName())
                .message("Registration received. Please check your email to verify your account.")
                .build();
    }
}