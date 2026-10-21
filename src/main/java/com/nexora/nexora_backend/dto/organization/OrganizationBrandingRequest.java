package com.nexora.nexora_backend.dto.organization;

import jakarta.validation.constraints.Size;

public record OrganizationBrandingRequest(
        Boolean useCompanyLogo,
        Boolean showNexoraAttribution,
        @Size(max = 500, message = "Invoice footer must be 500 characters or fewer")
        String invoiceFooterText,
        @Size(max = 500, message = "Receipt footer must be 500 characters or fewer")
        String receiptFooterText
) {}
