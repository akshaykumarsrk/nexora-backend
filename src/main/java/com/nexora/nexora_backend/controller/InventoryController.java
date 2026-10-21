package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.inventory.InventoryRequest;
import com.nexora.nexora_backend.dto.inventory.InventoryResponse;
import com.nexora.nexora_backend.dto.inventory.UpdateInventoryRequest;
import com.nexora.nexora_backend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {

        return ResponseEntity.ok(inventoryService.getAllInventory());
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/part/{partId}")
    public ResponseEntity<InventoryResponse> getInventoryByPartId(
            @PathVariable Long partId
    ) {

        return ResponseEntity.ok(inventoryService.getInventoryByPartId(partId));
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody InventoryRequest request
    ) {

        return new ResponseEntity<>(inventoryService.createInventory(request), HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest request
    ) {

        return new  ResponseEntity<>(inventoryService.updateInventory(id, request), HttpStatus.OK);
    }


    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateInventory(
            @PathVariable Long id
    ) {

        inventoryService.deactivateInventory(id);
        return ResponseEntity.noContent().build();
    }
}
