package com.nexora.nexora_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Redis-backed fixed-window rate limiter for public authentication and password-recovery endpoints.
 * Client IP is taken from the servlet remote address; forwarded headers are deliberately
 * not trusted unless a trusted-proxy strategy is configured separately.
 */
public class AuthenticationRateLimitFilter extends OncePerRequestFilter {

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]); " +
                    "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; " +
                    "if current > tonumber(ARGV[2]) then return redis.call('TTL', KEYS[1]); end; " +
                    "return -1;", Long.class);

    private final StringRedisTemplate redisTemplate;
    private final int registerLimit;
    private final int loginLimit;
    private final int windowSeconds;

    public AuthenticationRateLimitFilter(
            StringRedisTemplate redisTemplate,
            int registerLimit,
            int loginLimit,
            int windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.registerLimit = Math.max(1, registerLimit);
        this.loginLimit = Math.max(1, loginLimit);
        this.windowSeconds = Math.max(1, windowSeconds);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        return !"/api/auth/register".equals(path)
                && !"/api/auth/login".equals(path)
                && !"/api/auth/forgot-password".equals(path)
                && !"/api/auth/reset-password".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        int limit = "/api/auth/register".equals(path) ? registerLimit : loginLimit;
        String endpoint = switch (path) {
            case "/api/auth/register" -> "register";
            case "/api/auth/login" -> "login";
            case "/api/auth/forgot-password" -> "forgot-password";
            case "/api/auth/reset-password" -> "reset-password";
            default -> "auth";
        };
        String ipHash = sha256Hex(request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr());
        String key = "nexora:rate-limit:auth:" + endpoint + ":" + ipHash;

        final Long retryAfter;
        try {
            retryAfter = redisTemplate.execute(
                    RATE_LIMIT_SCRIPT, List.of(key), Integer.toString(windowSeconds), Integer.toString(limit));
        } catch (RuntimeException exception) {
            // Fail closed for public authentication endpoints if the shared limiter is unavailable.
            writeServiceUnavailable(response);
            return;
        }

        if (retryAfter == null) {
            writeServiceUnavailable(response);
            return;
        }
        if (retryAfter >= 0) {
            long seconds = Math.max(1L, retryAfter);
            response.setStatus(429);
            response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(seconds));
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"status\":429,\"message\":\"Too many authentication requests. Please try again later.\",\"errors\":null}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static void writeServiceUnavailable(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setHeader(HttpHeaders.RETRY_AFTER, "5");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":503,\"message\":\"Authentication protection is temporarily unavailable. Please retry shortly.\",\"errors\":null}");
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
