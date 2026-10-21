package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.InventoryConverter;
import com.nexora.nexora_backend.dto.inventory.InventoryRequest;
import com.nexora.nexora_backend.dto.inventory.InventoryResponse;
import com.nexora.nexora_backend.dto.inventory.UpdateInventoryRequest;
import com.nexora.nexora_backend.entity.Inventory;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Part;
import com.nexora.nexora_backend.entity.MovementType;
import com.nexora.nexora_backend.entity.StockMovement;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.InventoryRepository;
import com.nexora.nexora_backend.repository.PartRepository;
import com.nexora.nexora_backend.repository.StockMovementRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private final PartRepository partRepository;

    private final StockMovementRepository stockMovementRepository;

    private final OrganizationContext organizationContext;

    private final EntityManager entityManager;


    public List<InventoryResponse> getAllInventory() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return inventoryRepository
                .findAllByOrganizationId(organizationId)
                .stream()
                .map(
                        InventoryConverter::
                                inventoryToInventoryResponse
                )
                .toList();
    }


    public InventoryResponse getInventoryById(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Inventory inventory =
                inventoryRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found"
                                )
                        );

        return InventoryConverter
                .inventoryToInventoryResponse(
                        inventory
                );
    }


    public InventoryResponse getInventoryByPartId(
            Long partId
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Inventory inventory =
                inventoryRepository
                        .findByPartIdAndOrganizationId(
                                partId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found for this part"
                                )
                        );

        return InventoryConverter
                .inventoryToInventoryResponse(
                        inventory
                );
    }


    @Transactional
    public InventoryResponse createInventory(
            InventoryRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        if (inventoryRepository
                .existsByPartIdAndOrganizationId(
                        request.getPartId(),
                        organizationId
                )) {

            throw new IllegalArgumentException(
                    "Inventory already exists for this part"
            );
        }

        Part part =
                partRepository
                        .findByIdAndOrganizationId(
                                request.getPartId(),
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Part not found"
                                )
                        );

        if (!Boolean.TRUE.equals(
                part.getActive()
        )) {

            throw new IllegalArgumentException(
                    "Cannot create inventory for an inactive part"
            );
        }

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );

        Inventory inventory =
                InventoryConverter
                        .inventoryRequestToInventory(
                                request,
                                part
                        );

        inventory.setOrganization(
                organization
        );

        Inventory savedInventory =
                inventoryRepository.save(
                        inventory
                );

        // Record opening stock in the same transaction as inventory creation.
        // A zero opening balance does not need a movement record.
        int openingQuantity = savedInventory.getQuantity();
        if (openingQuantity > 0) {
            StockMovement openingMovement =
                    StockMovement.builder()
                            .organization(organization)
                            .inventory(savedInventory)
                            .movementType(MovementType.STOCK_IN)
                            .quantity(openingQuantity)
                            .quantityBefore(0)
                            .quantityAfter(openingQuantity)
                            .reason("Opening stock")
                            .build();

            stockMovementRepository.save(openingMovement);
        }

        return InventoryConverter
                .inventoryToInventoryResponse(
                        savedInventory
                );
    }


    public InventoryResponse updateInventory(
            Long id,
            UpdateInventoryRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Inventory inventory =
                inventoryRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found"
                                )
                        );

        InventoryConverter
                .updateInventoryFromRequest(
                        inventory,
                        request
                );

        Inventory updatedInventory =
                inventoryRepository.save(
                        inventory
                );

        return InventoryConverter
                .inventoryToInventoryResponse(
                        updatedInventory
                );
    }


    public void deactivateInventory(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Inventory inventory =
                inventoryRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory not found"
                                )
                        );

        inventory.setActive(false);

        inventoryRepository.save(
                inventory
        );
    }
}