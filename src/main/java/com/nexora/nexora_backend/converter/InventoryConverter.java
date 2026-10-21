package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.inventory.InventoryRequest;
import com.nexora.nexora_backend.dto.inventory.InventoryResponse;
import com.nexora.nexora_backend.dto.inventory.UpdateInventoryRequest;
import com.nexora.nexora_backend.entity.Inventory;
import com.nexora.nexora_backend.entity.Part;

public class InventoryConverter {

    public static Inventory inventoryRequestToInventory(
            InventoryRequest request,
            Part part
    ) {

        return Inventory.builder()
                .part(part)
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .reorderLevel(request.getReorderLevel())
                .active(true)
                .build();
    }

    public static InventoryResponse inventoryToInventoryResponse(
            Inventory inventory
    ) {

        int quantity = inventory.getQuantity();

        int reservedQuantity =
                inventory.getReservedQuantity();

        int availableQuantity =
                quantity - reservedQuantity;

        return InventoryResponse.builder()
                .id(inventory.getId())
                .partId(inventory.getPart().getId())
                .partCode(inventory.getPart().getPartCode())
                .partName(inventory.getPart().getName())
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .availableQuantity(availableQuantity)
                .reorderLevel(inventory.getReorderLevel())
                .lowStock(
                        availableQuantity
                                <= inventory.getReorderLevel()
                )
                .active(inventory.getActive())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    public static void updateInventoryFromRequest(
            Inventory inventory,
            UpdateInventoryRequest request
    ) {

        inventory.setReorderLevel(
                request.getReorderLevel()
        );
    }
}