package com.nexora.nexora_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.nexora_backend.dto.ApiError;
import com.nexora.nexora_backend.security.AuthenticationRateLimitFilter;
import com.nexora.nexora_backend.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    AuthenticationRateLimitFilter authenticationRateLimitFilter(
            org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
            @Value("${nexora.rate-limit.register.max-requests:5}") int registerLimit,
            @Value("${nexora.rate-limit.login.max-requests:10}") int loginLimit,
            @Value("${nexora.rate-limit.window-seconds:900}") int windowSeconds
    ) {
        return new AuthenticationRateLimitFilter(
                redisTemplate,
                registerLimit,
                loginLimit,
                windowSeconds
        );
    }

    @Bean
    FilterRegistrationBean<AuthenticationRateLimitFilter>
    disableRateLimitContainerRegistration(
            AuthenticationRateLimitFilter filter
    ) {
        FilterRegistrationBean<AuthenticationRateLimitFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter>
    disableJwtContainerRegistration(
            JwtAuthenticationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);
        return registration;
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        ObjectMapper objectMapper = new ObjectMapper();

        return (request, response, exception) -> {
            writeSecurityError(
                    response,
                    objectMapper,
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required or the token is invalid"
            );
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        ObjectMapper objectMapper = new ObjectMapper();

        return (request, response, exception) -> {
            writeSecurityError(
                    response,
                    objectMapper,
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to perform this operation"
            );
        };
    }

    private void writeSecurityError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message
    ) throws java.io.IOException {

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

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthenticationRateLimitFilter authenticationRateLimitFilter,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())

                .addFilterBefore(
                        authenticationRateLimitFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}


// Why OPTIONS is permitted
// Think of the browser doing:
// Browser
//   │
//   │ OPTIONS /api/employees
//   │ "Can I send this request?"
//   ↓
// Spring Security
//   │
//   └── Allow it ✅
//           │
//           ↓
// GET /api/employees
// Authorization: Bearer JWT
//           │
//           ↓
// JWT Filter
//           │
//           ↓
// EmployeeController


// Why OPTIONS is permitted
// Think of the browser doing:
// Browser
//   │
//   │ OPTIONS /api/employees
//   │ "Can I send this request?"
//   ↓
// Spring Security
//   │
//   └── Allow it ✅
//           │
//           ↓
// GET /api/employees
// Authorization: Bearer JWT
//           │
//           ↓
// JWT Filter
//           │
//           ↓
// EmployeeController
