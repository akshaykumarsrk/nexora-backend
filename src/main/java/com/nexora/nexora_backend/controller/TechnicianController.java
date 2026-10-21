package com.nexora.nexora_backend.controller;

import org.springframework.http.ResponseEntity;
import com.nexora.nexora_backend.dto.technician.TechnicianRequest;
import com.nexora.nexora_backend.dto.technician.TechnicianResponse;
import com.nexora.nexora_backend.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<TechnicianResponse>> getAllTechnicians() {

        return ResponseEntity.ok(
                technicianService.getAllTechnicians()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponse> getTechnicianById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                technicianService.getTechnicianById(id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<TechnicianResponse> createTechnician(
            @Valid @RequestBody TechnicianRequest request
    ) {

        return ResponseEntity.ok(
                technicianService.createTechnician(request)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<TechnicianResponse> updateTechnician(
            @PathVariable Long id,
            @Valid @RequestBody TechnicianRequest request
    ) {

        return ResponseEntity.ok(
                technicianService.updateTechnician(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTechnician(
            @PathVariable Long id
    ) {

        technicianService.deleteTechnician(id);
        return ResponseEntity.noContent().build();
    }
}
