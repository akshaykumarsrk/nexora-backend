package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.StockMovementConverter;
import com.nexora.nexora_backend.dto.inventory.StockMovementRequest;
import com.nexora.nexora_backend.dto.inventory.StockMovementResponse;
import com.nexora.nexora_backend.entity.Inventory;
import com.nexora.nexora_backend.entity.MovementType;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.StockMovement;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.InventoryRepository;
import com.nexora.nexora_backend.repository.StockMovementRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    private final InventoryRepository inventoryRepository;

    private final OrganizationContext organizationContext;

    private final EntityManager entityManager;


    // =========================================================
    // CREATE MANUAL STOCK MOVEMENT
    // =========================================================

    @Transactional
    public StockMovementResponse createMovement(
            StockMovementRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        Inventory inventory =
                inventoryRepository
                        .findWithLockByIdAndOrganizationId(
                                request.getInventoryId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found"
                                )
                        );


        if (!Boolean.TRUE.equals(
                inventory.getActive()
        )) {

            throw new IllegalArgumentException(
                    "Inventory is inactive"
            );
        }


        int quantityBefore = inventory.getQuantity();
        int quantityAfter;
        int movementQuantity = request.getQuantity();

        // STOCK IN: quantity is the amount being received.
        if (request.getMovementType() == MovementType.STOCK_IN) {
            requirePositiveMovementQuantity(request.getQuantity());
            try {
                quantityAfter = Math.addExact(quantityBefore, request.getQuantity());
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("Stock quantity exceeds the supported limit");
            }
        }
        // STOCK OUT: quantity is the amount being removed.
        else if (request.getMovementType() == MovementType.STOCK_OUT) {
            requirePositiveMovementQuantity(request.getQuantity());

            int availableQuantity = quantityBefore - inventory.getReservedQuantity();
            if (request.getQuantity() > availableQuantity) {
                throw new IllegalArgumentException(
                        "Insufficient stock. Available: " + availableQuantity
                );
            }

            quantityAfter = quantityBefore - request.getQuantity();
        }
        // ADJUSTMENT: quantity is the target physical stock count, not a delta.
        else if (request.getMovementType() == MovementType.ADJUSTMENT) {
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new IllegalArgumentException(
                        "A reason is required for stock adjustments"
                );
            }

            quantityAfter = request.getQuantity();

            if (quantityAfter < 0) {
                throw new IllegalArgumentException(
                        "Adjusted stock cannot be negative"
                );
            }

            if (quantityAfter < inventory.getReservedQuantity()) {
                throw new IllegalArgumentException(
                        "Adjusted stock cannot be less than reserved quantity: "
                                + inventory.getReservedQuantity()
                );
            }

            if (quantityAfter == quantityBefore) {
                throw new IllegalArgumentException(
                        "Adjusted quantity is the same as current stock"
                );
            }

            // Store the absolute quantity difference; before/after show direction.
            movementQuantity = Math.abs(quantityAfter - quantityBefore);
        } else {
            throw new IllegalArgumentException("Unsupported stock movement type");
        }


        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );


        StockMovement movement =
                StockMovement.builder()
                        .organization(organization)
                        .inventory(inventory)
                        .movementType(
                                request.getMovementType()
                        )
                        .quantity(
                                movementQuantity
                        )
                        .quantityBefore(
                                quantityBefore
                        )
                        .quantityAfter(
                                quantityAfter
                        )
                        .reason(
                                request.getReason()
                        )
                        .jobId(
                                request.getJobId()
                        )
                        .technicianId(
                                request.getTechnicianId()
                        )
                        .build();


        inventory.setQuantity(
                quantityAfter
        );


        StockMovement savedMovement =
                stockMovementRepository.save(
                        movement
                );


        return StockMovementConverter
                .stockMovementToResponse(
                        savedMovement
                );
    }


    private void requirePositiveMovementQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero for stock-in and stock-out"
            );
        }
    }


    // =========================================================
    // STOCK OUT FOR JOB
    // =========================================================

    @Transactional
    public StockMovementResponse stockOutForJob(
            Long inventoryId,
            Integer quantity,
            String reason,
            Long jobId,
            Long technicianId
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        // -------------------------------------------------
        // 1. Find inventory
        // -------------------------------------------------

        Inventory inventory =
                inventoryRepository
                        .findWithLockByIdAndOrganizationId(
                                inventoryId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found"
                                )
                        );


        // -------------------------------------------------
        // 2. Check inventory active
        // -------------------------------------------------

        if (!Boolean.TRUE.equals(
                inventory.getActive()
        )) {

            throw new IllegalArgumentException(
                    "Inventory is inactive"
            );
        }


        // -------------------------------------------------
        // 3. Check quantity
        // -------------------------------------------------

        if (quantity == null || quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }


        // -------------------------------------------------
        // 4. Calculate available quantity
        // -------------------------------------------------

        int availableQuantity =
                inventory.getQuantity()
                        - inventory.getReservedQuantity();


        if (quantity > availableQuantity) {

            throw new IllegalArgumentException(
                    "Insufficient stock. Available: "
                            + availableQuantity
            );
        }


        // -------------------------------------------------
        // 5. Calculate stock after movement
        // -------------------------------------------------

        int quantityBefore =
                inventory.getQuantity();

        int quantityAfter =
                quantityBefore - quantity;


        // -------------------------------------------------
        // 6. Get organization reference
        // -------------------------------------------------

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );


        // -------------------------------------------------
        // 7. Create STOCK_OUT movement
        // -------------------------------------------------

        StockMovement movement =
                StockMovement.builder()
                        .organization(organization)
                        .inventory(inventory)
                        .movementType(
                                MovementType.STOCK_OUT
                        )
                        .quantity(quantity)
                        .quantityBefore(
                                quantityBefore
                        )
                        .quantityAfter(
                                quantityAfter
                        )
                        .reason(reason)
                        .jobId(jobId)
                        .technicianId(technicianId)
                        .build();


        // -------------------------------------------------
        // 8. Update inventory
        // -------------------------------------------------

        inventory.setQuantity(
                quantityAfter
        );


        // -------------------------------------------------
        // 9. Save movement
        // -------------------------------------------------

        StockMovement savedMovement =
                stockMovementRepository.save(
                        movement
                );


        return StockMovementConverter
                .stockMovementToResponse(
                        savedMovement
                );
    }


    // =========================================================
    // GET ALL MOVEMENTS
    // =========================================================

    public List<StockMovementResponse> getAllMovements() {

        Long organizationId =
                organizationContext.getOrganizationId();


        return stockMovementRepository
                .findAllByOrganizationIdOrderByCreatedAtDesc(
                        organizationId
                )
                .stream()
                .map(
                        StockMovementConverter::
                                stockMovementToResponse
                )
                .toList();
    }


    // =========================================================
    // GET MOVEMENTS BY INVENTORY
    // =========================================================

    public List<StockMovementResponse> getMovementsByInventory(
            Long inventoryId
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();


        // Make sure inventory belongs
        // to current organization.

        inventoryRepository
                .findByIdAndOrganizationId(
                        inventoryId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found"
                        )
                );


        return stockMovementRepository
                .findAllByInventoryIdAndOrganizationIdOrderByCreatedAtDesc(
                        inventoryId,
                        organizationId
                )
                .stream()
                .map(
                        StockMovementConverter::
                                stockMovementToResponse
                )
                .toList();
    }

    @Transactional
    public StockMovementResponse stockInForJob(
            Long inventoryId,
            Integer quantity,
            String reason,
            Long jobId,
            Long technicianId
    ) {
        Long organizationId = organizationContext.getOrganizationId();

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        Inventory inventory = inventoryRepository
                .findWithLockByIdAndOrganizationId(inventoryId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found"
                ));

        int quantityBefore = inventory.getQuantity();
        int quantityAfter;

        try {
            quantityAfter = Math.addExact(quantityBefore, quantity);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Stock quantity exceeds the supported limit"
            );
        }

        Organization organization = entityManager.getReference(
                Organization.class,
                organizationId
        );

        StockMovement movement = StockMovement.builder()
                .organization(organization)
                .inventory(inventory)
                .movementType(MovementType.STOCK_IN)
                .quantity(quantity)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .reason(reason)
                .jobId(jobId)
                .technicianId(technicianId)
                .build();

        inventory.setQuantity(quantityAfter);

        StockMovement savedMovement = stockMovementRepository.save(movement);

        return StockMovementConverter.stockMovementToResponse(savedMovement);
    }
}