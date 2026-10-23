
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


    /** Builds a simple responsive HTML wrapper for every transactional email. */
    private String buildBrandedHtml(String textContent) {
        String logoUrl = frontendUrl.replaceAll("/+$", "") + "/icon-192.png";
        String safeBody = escapeHtml(textContent).replace("\r\n", "\n")
                .replace("\r", "\n").replace("\n", "<br>\n");
        // Keep verification, reset, invitation and workspace URLs clickable in HTML mail.
        safeBody = safeBody.replaceAll("(https?://[^\\s<]+)",
                "<a href=\"$1\" style=\"color:#2563eb;text-decoration:underline;overflow-wrap:anywhere;\">$1</a>");
        String safeSender = escapeHtml(senderName);
        return """
                <!doctype html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                <body style="margin:0;padding:24px 12px;background:#f3f6fb;font-family:Arial,Helvetica,sans-serif;color:#172033;">
                  <div style="max-width:600px;margin:0 auto;background:#ffffff;border:1px solid #e2e8f0;border-radius:16px;overflow:hidden;">
                    <div style="padding:24px 28px;background:#0c1830;color:#ffffff;">
                      <table role="presentation" cellpadding="0" cellspacing="0" border="0"><tr>
                        <td style="vertical-align:middle;padding-right:12px;"><img src="{{LOGO_URL}}" width="42" height="42" alt="NEXORA" style="display:block;border:0;border-radius:10px;"></td>
                        <td style="vertical-align:middle;"><div style="font-size:20px;font-weight:700;letter-spacing:.4px;">NEXORA</div><div style="margin-top:4px;font-size:12px;color:#b8c7e6;">Business &amp; Field Service Management</div></td>
                      </tr></table>
                    </div>
                    <div style="padding:28px;font-size:14px;line-height:1.8;overflow-wrap:anywhere;">{{BODY}}</div>
                    <div style="padding:16px 28px;background:#f8fafc;border-top:1px solid #e2e8f0;font-size:12px;color:#64748b;">Sent by {{SENDER}} · Please do not share verification or password-reset links.</div>
                  </div>
                </body></html>
                """.replace("{{LOGO_URL}}", escapeHtml(logoUrl))
                .replace("{{BODY}}", safeBody)
                .replace("{{SENDER}}", safeSender);
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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
                "textContent", textContent,
                "htmlContent", buildBrandedHtml(textContent)
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
