package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.entity.EmailVerificationToken;
import com.nexora.nexora_backend.entity.User;
import com.nexora.nexora_backend.repository.EmailVerificationTokenRepository;
import com.nexora.nexora_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${nexora.verification.expiry-minutes:30}")
    private long expiryMinutes;

    @Transactional
    public void createAndSendVerificationEmail(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "A valid email address is required"
            );
        }

        if (user.isEmailVerified()) {
            return;
        }

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        Instant now = Instant.now();

        tokenRepository.invalidateUnusedTokens(user.getId(), now);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(now.plus(expiryMinutes, ChronoUnit.MINUTES))
                .build();

        tokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), rawToken);
    }

    @Transactional
    public void verifyEmail(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw invalidTokenException();
        }

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(this::invalidTokenException);

        if (!token.isUsable()) {
            throw invalidTokenException();
        }

        User user = token.getUser();

        if (user.isEmailVerified()) {
            throw invalidTokenException();
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);

        // Invalidate any other outstanding verification tokens.
        tokenRepository.invalidateUnusedTokens(
                user.getId(),
                Instant.now()
        );
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return java.util.HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    private ResponseStatusException invalidTokenException() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Verification link is invalid, expired, or already used"
        );
    }
}