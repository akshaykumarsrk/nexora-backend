package com.nexora.nexora_backend.controller;

import org.springframework.http.ResponseEntity;
import com.nexora.nexora_backend.dto.part.PartRequest;
import com.nexora.nexora_backend.dto.part.PartResponse;
import com.nexora.nexora_backend.service.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<PartResponse>> getAllParts() {

        return ResponseEntity.ok(
                partService.getAllParts()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<PartResponse> getPartById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                partService.getPartById(id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<PartResponse> createPart(
            @Valid @RequestBody PartRequest request
    ) {

        return ResponseEntity.ok(
                partService.createPart(request)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<PartResponse> updatePart(
            @PathVariable Long id,
            @Valid @RequestBody PartRequest request
    ) {

        return ResponseEntity.ok(
                partService.updatePart(
                id,
                request
        )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePart(
            @PathVariable Long id
    ) {

        partService.deletePart(id);
        return ResponseEntity.noContent().build();
    }
}
