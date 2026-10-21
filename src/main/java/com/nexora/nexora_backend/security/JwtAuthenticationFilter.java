package com.nexora.nexora_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.nexora_backend.dto.ApiError;
import com.nexora.nexora_backend.entity.User;
import com.nexora.nexora_backend.service.CustomUserDetailsService;
import com.nexora.nexora_backend.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            rejectRequest(response, "Invalid or expired authentication token");
            return;
        }

        final String username;
        final Long tokenOrganizationId;

        try {
            if (!jwtService.isTokenValid(token)) {
                rejectRequest(response, "Invalid or expired authentication token");
                return;
            }

            username = jwtService.extractUsername(token);
            tokenOrganizationId = jwtService.extractOrganizationId(token);

            if (username == null || username.isBlank()
                    || tokenOrganizationId == null
                    || tokenOrganizationId <= 0) {
                rejectRequest(response, "Invalid authentication token");
                return;
            }

        } catch (JwtException | IllegalArgumentException exception) {
            rejectRequest(response, "Invalid or expired authentication token");
            return;
        }

        final User user;

        try {
            user = userDetailsService.loadUserEntityByUsername(username);
        } catch (UsernameNotFoundException exception) {
            rejectRequest(response, "Invalid authentication token");
            return;
        }

        Long currentOrganizationId =
                user.getOrganization() != null
                        ? user.getOrganization().getId()
                        : null;

        if (!user.isEnabled()) {
            rejectRequest(response, "This account is deactivated");
            return;
        }

        if (currentOrganizationId == null
                || !currentOrganizationId.equals(tokenOrganizationId)) {
            rejectRequest(response, "Invalid organization in authentication token");
            return;
        }

        var authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getUsername(),
                user.getPassword(),
                authorities,
                currentOrganizationId
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authenticatedUser.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean passwordChangeEndpoint =
                "PATCH".equalsIgnoreCase(request.getMethod())
                        && "/api/users/me/password".equals(request.getServletPath());

        if (user.isMustChangePassword() && !passwordChangeEndpoint) {
            SecurityContextHolder.clearContext();

            rejectForbidden(
                    response,
                    "Password change required before accessing this resource"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void rejectRequest(
            HttpServletResponse response,
            String message
    ) throws IOException {
        SecurityContextHolder.clearContext();
        writeError(response, HttpStatus.UNAUTHORIZED, message);
    }

    private void rejectForbidden(
            HttpServletResponse response,
            String message
    ) throws IOException {
        writeError(response, HttpStatus.FORBIDDEN, message);
    }

    private void writeError(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiError apiError = new ApiError(
                status.value(),
                message,
                null
        );

        objectMapper.writeValue(response.getOutputStream(), apiError);
    }
}