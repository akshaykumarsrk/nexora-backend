package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.site.SiteRequest;
import com.nexora.nexora_backend.dto.site.SiteResponse;
import com.nexora.nexora_backend.service.SiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping
    public ResponseEntity<List<SiteResponse>> getSites() {

        return ResponseEntity.ok(
                siteService.getSites()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<SiteResponse> getSite(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                siteService.getSite(id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @PostMapping
    public ResponseEntity<SiteResponse> createSite(
            @Valid @RequestBody SiteRequest request
    ) {

        SiteResponse response =
                siteService.createSite(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<SiteResponse> updateSite(
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest request
    ) {

        return ResponseEntity.ok(
                siteService.updateSite(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSite(
            @PathVariable Long id
    ) {

        siteService.deleteSite(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SiteResponse>> getSitesByCustomer(
            @PathVariable Long customerId
    ) {

        return ResponseEntity.ok(
                siteService.getSitesByCustomer(customerId)
        );
    }
}
