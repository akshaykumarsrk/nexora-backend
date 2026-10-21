package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.organization.OrganizationBrandingRequest;
import com.nexora.nexora_backend.dto.organization.OrganizationBrandingResponse;
import com.nexora.nexora_backend.service.OrganizationBrandingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/organization/branding")
@RequiredArgsConstructor
public class OrganizationBrandingController {
    private final OrganizationBrandingService brandingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<OrganizationBrandingResponse> getSettings() {
        return ResponseEntity.ok(brandingService.getSettings());
    }

    @GetMapping("/logo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<byte[]> getLogo() {
        byte[] bytes = brandingService.getCurrentLogoBytes();
        if (bytes == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .contentType(MediaType.parseMediaType(brandingService.getCurrentLogoContentType()))
                .body(bytes);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationBrandingResponse> updateSettings(
            @Valid @RequestBody OrganizationBrandingRequest request) {
        return ResponseEntity.ok(brandingService.updateSettings(request));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationBrandingResponse> uploadLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(brandingService.uploadLogo(file));
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationBrandingResponse> removeLogo() {
        return ResponseEntity.ok(brandingService.removeLogo());
    }
}
