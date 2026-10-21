
package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.dto.user.*;
import com.nexora.nexora_backend.entity.*;
import com.nexora.nexora_backend.repository.*;


import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserInvitationService {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("ADMIN", "MANAGER", "STAFF", "TECHNICIAN");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final TechnicianRepository technicianRepository;
    private final OrganizationContext organizationContext;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final EmailService emailService;

    @Transactional
    public UserInvitationResponse createInvitation(
            CreateUserInvitationRequest request) {

        Instant now = Instant.now();
        Long organizationId = organizationContext.getOrganizationId();

        String email = request.getEmail().trim()
                .toLowerCase(Locale.ROOT);
        String username = request.getUsername().trim();
        String role = request.getRole().trim()
                .toUpperCase(Locale.ROOT);

        if (!ALLOWED_ROLES.contains(role)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only MANAGER, STAFF and TECHNICIAN can be invited"
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account already exists for this email"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already in use"
            );
        }

        if (invitationRepository
                .existsByEmailIgnoreCaseAndAcceptedAtIsNullAndExpiresAtAfter(
                        email, now)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An active invitation already exists for this email"
            );
        }

        if (invitationRepository
                .existsByUsernameAndAcceptedAtIsNullAndExpiresAtAfter(
                        username, now)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An active invitation already exists for this username"
            );
        }

        Technician technician = null;

        if ("TECHNICIAN".equals(role)) {
            if (request.getTechnicianId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A technician profile is required for this role"
                );
            }

            technician = technicianRepository
                    .findByIdAndOrganizationId(
                            request.getTechnicianId(), organizationId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Technician profile not found"
                    ));

            if (!"ACTIVE".equalsIgnoreCase(technician.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Technician profile is not active"
                );
            }

            if (technician.getUser() != null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Technician profile is already linked to a user"
                );
            }

            if (invitationRepository
                    .existsByTechnicianIdAndAcceptedAtIsNullAndExpiresAtAfter(
                            technician.getId(), now)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "An active invitation already exists for this technician"
                );
            }

        } else if (request.getTechnicianId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Technician ID is only allowed for TECHNICIAN accounts"
            );
        }

        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        String tokenHash = sha256(rawToken);
        Instant expiry = now.plus(24, ChronoUnit.HOURS);

        UserInvitation invitation = UserInvitation.builder()
                .organization(entityManager.getReference(
                        Organization.class, organizationId))
                .technician(technician)
                .email(email)
                .username(username)
                .role(role)
                .tokenHash(tokenHash)
                .expiresAt(expiry)
                .createdAt(now)
                .build();

        invitationRepository.saveAndFlush(invitation);

        // If delivery fails, the transaction rolls back.
        emailService.sendInvitationEmail(email, rawToken, role);

        return UserInvitationResponse.builder()
                .message("Invitation email sent successfully")
                .expiresAt(expiry)
                .build();
    }

    @Transactional
    public AcceptUserInvitationResponse acceptInvitation(
            AcceptUserInvitationRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password exceeds the supported length"
            );
        }

        String tokenHash = sha256(request.getToken());

        UserInvitation invitation = invitationRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invitation is invalid or has expired"
                ));

        Instant now = Instant.now();

        if (invitation.getAcceptedAt() != null
                || !invitation.getExpiresAt().isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invitation is invalid or has expired"
            );
        }

        if (userRepository.existsByUsername(invitation.getUsername())
                || userRepository.existsByEmailIgnoreCase(invitation.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account already exists for this username or email"
            );
        }

        Technician technician = invitation.getTechnician();

        if ("TECHNICIAN".equals(invitation.getRole())) {
            if (technician == null
                    || !technician.getOrganization().getId()
                    .equals(invitation.getOrganization().getId())
                    || !"ACTIVE".equalsIgnoreCase(technician.getStatus())
                    || technician.getUser() != null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Technician profile is no longer available"
                );
            }
        }

        User user = new User();
        user.setUsername(invitation.getUsername());
        user.setEmail(invitation.getEmail());
        user.setEmailVerified(true);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(invitation.getRole());
        user.setOrganization(invitation.getOrganization());
        user.setEnabled(true);
        user.setMustChangePassword(false);

        try {
            User savedUser = userRepository.saveAndFlush(user);

            if (technician != null) {
                technician.setUser(savedUser);
                technicianRepository.save(technician);
            }

            invitation.setAcceptedAt(now);
            invitationRepository.save(invitation);

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Account or technician profile conflicts with existing data"
            );
        }

        return new AcceptUserInvitationResponse(
                "Your NEXORA account has been activated. You can now sign in."
        );
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8));

            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is not available", ex);
        }
    }
}
