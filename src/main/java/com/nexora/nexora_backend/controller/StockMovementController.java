package com.nexora.nexora_backend.controller;

import org.springframework.http.ResponseEntity;
import com.nexora.nexora_backend.dto.inventory.StockMovementRequest;
import com.nexora.nexora_backend.dto.inventory.StockMovementResponse;
import com.nexora.nexora_backend.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<StockMovementResponse>> getAllMovements() {

        return ResponseEntity.ok(
                stockMovementService
                .getAllMovements()
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<List<StockMovementResponse>>
    getMovementsByInventory(
            @PathVariable Long inventoryId
    ) {

        return ResponseEntity.ok(
                stockMovementService
                .getMovementsByInventory(
                        inventoryId
                )
        );
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockMovementResponse> createMovement(
            @Valid @RequestBody StockMovementRequest request
    ) {

        return ResponseEntity.ok(
                stockMovementService
                .createMovement(request)
        );
    }
}
