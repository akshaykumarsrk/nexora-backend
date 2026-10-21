package com.nexora.nexora_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${nexora.mail.from}")
    private String fromEmail;

    @Value("${nexora.mail.sender-name:NEXORA}")
    private String senderName;

    @Value("${nexora.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public void sendVerificationEmail(
            String recipientEmail,
            String rawToken
    ) {
        String verificationUrl =
                frontendUrl.replaceAll("/+$", "")
                        + "/verify-email?token="
                        + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("Verify your NEXORA email address");

        message.setText("""
                Welcome to NEXORA!

                Please verify your email address using the link below:

                %s

                This link expires in 30 minutes and can only be used once.

                If you did not create this account, you can ignore this email.

                Regards,
                %s Team
                """.formatted(verificationUrl, senderName));

        mailSender.send(message);
    }


    public void sendPasswordResetEmail(String recipientEmail, String rawToken) {
        String resetUrl = frontendUrl.replaceAll("/+$", "") + "/reset-password?token="
                + java.net.URLEncoder.encode(rawToken, java.nio.charset.StandardCharsets.UTF_8);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("Reset your NEXORA password");
        message.setText("""
                We received a request to reset your NEXORA password.

                Use this link within 30 minutes to choose a new password:
                %s

                If you did not request this, you can ignore this email.
                Your password will not change unless the link is used.

                Regards,
                %s Team
                """.formatted(resetUrl, senderName));
        mailSender.send(message);
    }

    public void sendInvitationEmail(
            String recipientEmail,
            String rawToken,
            String invitedRole) {

        String invitationUrl = frontendUrl.replaceAll("/+$", "")
                + "/accept-invitation?token="
                + java.net.URLEncoder.encode(
                rawToken,
                java.nio.charset.StandardCharsets.UTF_8
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("Your NEXORA employee invitation");

        message.setText("""
        You have been invited to join NEXORA as %s.

        Use the link below to set your password and activate your account:

        %s

        This invitation expires in 24 hours and can only be used once.

        If you were not expecting this invitation, you can ignore this email.

        Regards,
        %s Team
        """.formatted(invitedRole, invitationUrl, senderName));

        mailSender.send(message);
    }


    public void sendTechnicianJobAssignmentEmail(String recipientEmail, String jobNumber, String title, String scheduledAt) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("New service job assigned: " + jobNumber);
        message.setText("""
                A new service job has been assigned to you in NEXORA.

                Job: %s
                Description: %s
                Schedule: %s

                Sign in to your NEXORA technician workspace to review the assignment and job details:
                %s/technician-dashboard

                Regards,
                %s Team
                """.formatted(jobNumber, title == null ? "Service job" : title, scheduledAt, frontendUrl.replaceAll("/+$", ""), senderName));
        mailSender.send(message);
    }
}