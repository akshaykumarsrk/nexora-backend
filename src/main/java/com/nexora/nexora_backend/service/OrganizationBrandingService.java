package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.dto.organization.OrganizationBrandingRequest;
import com.nexora.nexora_backend.dto.organization.OrganizationBrandingResponse;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.OrganizationRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationBrandingService {
    private static final long MAX_LOGO_BYTES = 2L * 1024 * 1024;

    private final OrganizationRepository organizationRepository;
    private final OrganizationContext organizationContext;

    @Value("${nexora.storage.logo-directory:./data/organization-logos}")
    private String logoDirectory;

    @Transactional(readOnly = true)
    public OrganizationBrandingResponse getSettings() {
        return toResponse(getCurrentOrganization());
    }

    @Transactional
    public OrganizationBrandingResponse updateSettings(OrganizationBrandingRequest request) {
        Organization organization = getCurrentOrganization();
        if (request.useCompanyLogo() != null) organization.setUseCompanyLogo(request.useCompanyLogo());
        if (request.showNexoraAttribution() != null) organization.setShowNexoraAttribution(request.showNexoraAttribution());
        if (request.invoiceFooterText() != null) organization.setInvoiceFooterText(clean(request.invoiceFooterText()));
        if (request.receiptFooterText() != null) organization.setReceiptFooterText(clean(request.receiptFooterText()));
        return toResponse(organizationRepository.save(organization));
    }

    @Transactional
    public OrganizationBrandingResponse uploadLogo(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose a logo image to upload");
        if (file.getSize() > MAX_LOGO_BYTES) throw new IllegalArgumentException("Logo must be 2 MB or smaller");

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            default -> throw new IllegalArgumentException("Only PNG and JPEG logo images are supported");
        };

        try {
            BufferedImage image;
            try (var inputStream = file.getInputStream()) {
                image = ImageIO.read(inputStream);
            }
            if (image == null || image.getWidth() < 1 || image.getHeight() < 1
                    || image.getWidth() > 5000 || image.getHeight() > 5000) {
                throw new IllegalArgumentException("Uploaded file is not a valid supported image");
            }

            Organization organization = getCurrentOrganization();
            Path root = Paths.get(logoDirectory).toAbsolutePath().normalize();
            Path organizationDirectory = root.resolve(String.valueOf(organization.getId())).normalize();
            if (!organizationDirectory.startsWith(root)) throw new IllegalStateException("Invalid logo storage path");
            Files.createDirectories(organizationDirectory);

            String filename = UUID.randomUUID() + "." + extension;
            Path destination = organizationDirectory.resolve(filename).normalize();
            if (!destination.startsWith(organizationDirectory)) throw new IllegalStateException("Invalid logo filename");
            file.transferTo(destination);

            organization.setLogoStorageKey(organization.getId() + "/" + filename);
            organization.setUseCompanyLogo(true);
            organizationRepository.save(organization);
            // Keep replaced files until a post-commit cleanup job exists; deleting before
            // transaction commit could leave the database pointing at a missing logo.
            return toResponse(organization);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store company logo", exception);
        }
    }

    @Transactional
    public OrganizationBrandingResponse removeLogo() {
        Organization organization = getCurrentOrganization();
        organization.setLogoStorageKey(null);
        organization.setUseCompanyLogo(false);
        return toResponse(organizationRepository.save(organization));
    }

    /** Returns bytes only for the authenticated organization's stored logo. */
    @Transactional(readOnly = true)
    public byte[] getCurrentLogoBytes() {
        return getLogoBytesForOrganization(organizationContext.getOrganizationId());
    }

    /**
     * Reads a logo for a specific organization. Document generators should use
     * the organization attached to the document rather than relying on ambient
     * request context, so tenant branding cannot accidentally cross over.
     */
    @Transactional(readOnly = true)
    public byte[] getLogoBytesForOrganization(Long organizationId) {
        if (organizationId == null) return null;
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (!organization.isUseCompanyLogo() || organization.getLogoStorageKey() == null) return null;
        Path root = Paths.get(logoDirectory).toAbsolutePath().normalize();
        Path file = root.resolve(organization.getLogoStorageKey()).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) return null;
        try { return Files.readAllBytes(file); }
        catch (IOException exception) { throw new IllegalStateException("Unable to read company logo", exception); }
    }

    @Transactional(readOnly = true)
    public String getCurrentLogoContentType() {
        Organization organization = getCurrentOrganization();
        if (organization.getLogoStorageKey() == null) return "application/octet-stream";
        return organization.getLogoStorageKey().toLowerCase(Locale.ROOT).endsWith(".png")
                ? "image/png" : "image/jpeg";
    }

    private Organization getCurrentOrganization() {
        Long organizationId = organizationContext.getOrganizationId();
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    private OrganizationBrandingResponse toResponse(Organization organization) {
        String logoUrl = !organization.isUseCompanyLogo() || organization.getLogoStorageKey() == null
                ? null : "/api/organization/branding/logo";
        return new OrganizationBrandingResponse(organization.getName(), logoUrl,
                organization.isUseCompanyLogo(), organization.isShowNexoraAttribution(),
                organization.getInvoiceFooterText(), organization.getReceiptFooterText());
    }

    private String clean(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
