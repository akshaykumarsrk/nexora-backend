package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.asset.AssetRequest;
import com.nexora.nexora_backend.dto.asset.AssetResponse;
import com.nexora.nexora_backend.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets() {

        return ResponseEntity.ok(
                assetService.getAssets()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> getAsset(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                assetService.getAsset(id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<AssetResponse> createAsset(
            @Valid @RequestBody AssetRequest request
    ) {

        AssetResponse response =
                assetService.createAsset(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<AssetResponse> updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request
    ) {

        return ResponseEntity.ok(
                assetService.updateAsset(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAsset(
            @PathVariable Long id
    ) {

        assetService.deleteAsset(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/site/{siteId}")
    public ResponseEntity<List<AssetResponse>> getAssetsBySite(
            @PathVariable Long siteId
    ) {

        return ResponseEntity.ok(
                assetService.getAssetsBySite(siteId)
        );
    }
}
