package com.nexora.nexora_backend.dto.organization;

public record OrganizationBrandingResponse(
        String organizationName,
        String logoUrl,
        boolean useCompanyLogo,
        boolean showNexoraAttribution,
        String invoiceFooterText,
        String receiptFooterText
) {}
