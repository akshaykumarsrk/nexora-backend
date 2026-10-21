package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.entity.PasswordResetToken;
import com.nexora.nexora_backend.entity.User;
import com.nexora.nexora_backend.repository.PasswordResetTokenRepository;
import com.nexora.nexora_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${nexora.password-reset.expiry-minutes:30}")
    private long expiryMinutes;

    @Transactional
    public void requestReset(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(normalized)
                .ifPresent(user -> {
                    // Only verified, enabled accounts may initiate a password reset.
                    if (!user.isEmailVerified() || !user.isEnabled()) return;
                    Instant now = Instant.now();
                    tokenRepository.invalidateUnusedTokens(user.getId(), now);
                    byte[] bytes = new byte[32];
                    RANDOM.nextBytes(bytes);
                    String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                    tokenRepository.save(PasswordResetToken.builder()
                            .user(user).tokenHash(hash(raw))
                            .expiresAt(now.plus(Math.max(5, Math.min(expiryMinutes, 60)), ChronoUnit.MINUTES))
                            .build());
                    try {
                        emailService.sendPasswordResetEmail(user.getEmail(), raw);
                    } catch (RuntimeException mailFailure) {
                        // Keep the public response non-enumerating; operational logs retain the failure.
                        log.error("Password reset email could not be sent for eligible account id {}", user.getId(), mailFailure);
                    }
                });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank() || newPassword == null || newPassword.isBlank()
                || newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw invalidToken();
        }
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(this::invalidToken);
        if (!token.isUsable()) throw invalidToken();
        User user = token.getUser();
        if (!user.isEnabled() || !user.isEmailVerified()) throw invalidToken();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        Instant now = Instant.now();
        token.setUsedAt(now);
        tokenRepository.save(token);
        tokenRepository.invalidateUnusedTokens(user.getId(), now);
    }

    private String hash(String token) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 unavailable", ex); }
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset link is invalid, expired, or already used");
    }
}
