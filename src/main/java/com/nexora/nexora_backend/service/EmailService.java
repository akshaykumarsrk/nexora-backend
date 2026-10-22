
package com.nexora.nexora_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String BREVO_API_URL =
            "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${nexora.mail.from}")
    private String fromEmail;

    @Value("${nexora.mail.sender-name:NEXORA}")
    private String senderName;

    @Value("${nexora.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${nexora.mail.brevo-api-key}")
    private String brevoApiKey;

    public void sendVerificationEmail(
            String recipientEmail, String rawToken) {

        String verificationUrl = frontendUrl.replaceAll("/+$", "")
                + "/verify-email?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        sendEmail(
                recipientEmail,
                "Verify your NEXORA email address",
                """
                Welcome to NEXORA!

                Please verify your email address using this link:

                %s

                This link expires in 30 minutes and can only be used once.

                If you did not create this account, you can ignore this email.

                Regards,
                %s Team
                """.formatted(verificationUrl, senderName)
        );
    }

    public void sendPasswordResetEmail(
            String recipientEmail, String rawToken) {

        String resetUrl = frontendUrl.replaceAll("/+$", "")
                + "/reset-password?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        sendEmail(
                recipientEmail,
                "Reset your NEXORA password",
                """
                We received a request to reset your NEXORA password.

                Use this link within 30 minutes to choose a new password:

                %s

                If you did not request this, you can ignore this email.
                Your password will not change unless the link is used.

                Regards,
                %s Team
                """.formatted(resetUrl, senderName)
        );
    }

    public void sendInvitationEmail(
            String recipientEmail,
            String rawToken,
            String invitedRole) {

        String invitationUrl = frontendUrl.replaceAll("/+$", "")
                + "/accept-invitation?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        sendEmail(
                recipientEmail,
                "Your NEXORA employee invitation",
                """
                You have been invited to join NEXORA as %s.

                Use the link below to set your password and activate your account:

                %s

                This invitation expires in 24 hours and can only be used once.

                If you were not expecting this invitation, you can ignore this email.

                Regards,
                %s Team
                """.formatted(invitedRole, invitationUrl, senderName)
        );
    }

    public void sendTechnicianJobAssignmentEmail(
            String recipientEmail,
            String jobNumber,
            String title,
            String scheduledAt) {

        sendEmail(
                recipientEmail,
                "New service job assigned: " + jobNumber,
                """
                A new service job has been assigned to you in NEXORA.

                Job: %s
                Description: %s
                Schedule: %s

                Sign in to your NEXORA technician workspace:
                %s/technician-dashboard

                Regards,
                %s Team
                """.formatted(
                        jobNumber,
                        title == null ? "Service job" : title,
                        scheduledAt,
                        frontendUrl.replaceAll("/+$", ""),
                        senderName
                )
        );
    }

    private void sendEmail(
            String recipientEmail,
            String subject,
            String textContent) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", brevoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", senderName,
                        "email", fromEmail
                ),
                "to", List.of(
                        Map.of("email", recipientEmail)
                ),
                "subject", subject,
                "textContent", textContent
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(
                BREVO_API_URL,
                request,
                String.class
        );
    }
}
