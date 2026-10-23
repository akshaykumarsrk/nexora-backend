# NEXORA transactional branding update

- Transactional Brevo emails now include a responsive NEXORA-branded HTML wrapper while preserving the plain-text version.
- Links in verification, password-reset, invitation, and job-assignment emails remain clickable in HTML mail.
- Invoice and payment-receipt PDF generation now resolves the logo against the organization attached to the document, rather than relying only on request context. This helps keep tenant logos isolated.
- Existing organization-level logo settings, custom footer text, and the optional “Powered by NEXORA” attribution remain in place. When a company logo is disabled or missing, the PDF continues to identify the issuing business by its company name/details rather than falsely substituting NEXORA as the issuer.

## Git commands

```bash
git status
git add src/main/java/com/nexora/nexora_backend/service/EmailService.java src/main/java/com/nexora/nexora_backend/service/OrganizationBrandingService.java src/main/java/com/nexora/nexora_backend/service/InvoicePdfService.java src/main/java/com/nexora/nexora_backend/service/PaymentReceiptService.java
git commit -m "Add consistent NEXORA email and document branding"
git push origin main
```
