package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.catalog.EquipmentModelRequest;
import com.nexora.nexora_backend.dto.catalog.EquipmentModelResponse;
import com.nexora.nexora_backend.service.EquipmentModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/catalog/equipment-models") @RequiredArgsConstructor
public class EquipmentModelController {
    private final EquipmentModelService service;
    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')") public ResponseEntity<List<EquipmentModelResponse>> getAll(@RequestParam(required = false) Long brandId) { return ResponseEntity.ok(service.getAll(brandId)); }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')") public ResponseEntity<EquipmentModelResponse> getById(@PathVariable Long id) { return ResponseEntity.ok(service.getById(id)); }
    @PostMapping @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ResponseEntity<EquipmentModelResponse> create(@Valid @RequestBody EquipmentModelRequest request) { return ResponseEntity.ok(service.create(request)); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") public ResponseEntity<EquipmentModelResponse> update(@PathVariable Long id, @Valid @RequestBody EquipmentModelRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
