package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.inventory.StockMovementResponse;
import com.nexora.nexora_backend.entity.StockMovement;

public class StockMovementConverter {

    public static StockMovementResponse
    stockMovementToResponse(
            StockMovement movement
    ) {

        return StockMovementResponse.builder()
                .id(movement.getId())
                .inventoryId(
                        movement.getInventory().getId()
                )
                .partId(
                        movement.getInventory()
                                .getPart()
                                .getId()
                )
                .partCode(
                        movement.getInventory()
                                .getPart()
                                .getPartCode()
                )
                .partName(
                        movement.getInventory()
                                .getPart()
                                .getName()
                )
                .movementType(
                        movement.getMovementType()
                )
                .quantity(
                        movement.getQuantity()
                )
                .quantityBefore(
                        movement.getQuantityBefore()
                )
                .quantityAfter(
                        movement.getQuantityAfter()
                )
                .reason(
                        movement.getReason()
                )
                .jobId(
                        movement.getJobId()
                )
                .technicianId(
                        movement.getTechnicianId()
                )
                .createdAt(
                        movement.getCreatedAt()
                )
                .build();
    }
}