package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.catalog.BrandRequest;
import com.nexora.nexora_backend.dto.catalog.BrandResponse;
import com.nexora.nexora_backend.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/catalog/brands") @RequiredArgsConstructor
public class BrandController {
    private final BrandService service;
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')") public ResponseEntity<List<BrandResponse>> getAll() { return ResponseEntity.ok(service.getAll()); }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')") public ResponseEntity<BrandResponse> getById(@PathVariable Long id) { return ResponseEntity.ok(service.getById(id)); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) { return ResponseEntity.ok(service.create(request)); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ResponseEntity<BrandResponse> update(@PathVariable Long id, @Valid @RequestBody BrandRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
