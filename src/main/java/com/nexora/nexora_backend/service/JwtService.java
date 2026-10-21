package com.nexora.nexora_backend.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationTime) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret must not be empty"
            );
        }

        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes for HS256"
            );
        }

        if (expirationTime <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expirationTime = expirationTime;
    }

    public String generateToken(
            String username,
            String role,
            Long organizationId
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username must not be empty"
            );
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException(
                    "Role must not be empty"
            );
        }

        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException(
                    "Organization ID must be valid"
            );
        }

        long now = System.currentTimeMillis();

        final long expirationTimestamp;

        try {
            expirationTimestamp = Math.addExact(now, expirationTime);
        } catch (ArithmeticException exception) {
            throw new IllegalStateException(
                    "Configured JWT expiration is too large",
                    exception
            );
        }

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("organizationId", organizationId)
                .issuedAt(new Date(now))
                .expiration(new Date(expirationTimestamp))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            extractUsername(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    // This basically says:
    //"Open the JWT, verify it, go to its payload, find organizationId, and give me its value."
    public Long extractOrganizationId(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("organizationId", Long.class);
    }
}