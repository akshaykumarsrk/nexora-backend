package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.part.PartRequest;
import com.nexora.nexora_backend.dto.part.PartResponse;
import com.nexora.nexora_backend.entity.Part;

public class PartConverter {

    public static Part partRequestToPart(
            PartRequest request
    ) {

        return Part.builder()
                .partCode(request.getPartCode())
                .name(request.getName())
                .category(request.getCategory())
                .brand(request.getBrand())
                .unit(request.getUnit())
                .description(request.getDescription())
                .active(true)
                .build();
    }

    public static PartResponse partToPartResponse(
            Part part
    ) {

        return PartResponse.builder()
                .id(part.getId())
                .partCode(part.getPartCode())
                .name(part.getName())
                .category(part.getCategory())
                .brand(part.getBrand())
                .unit(part.getUnit())
                .active(part.getActive())
                .description(part.getDescription())
                .createdAt(part.getCreatedAt())
                .updatedAt(part.getUpdatedAt())
                .build();
    }

    public static void updatePartFromRequest(
            Part part,
            PartRequest request
    ) {

        part.setPartCode(request.getPartCode());
        part.setName(request.getName());
        part.setCategory(request.getCategory());
        part.setBrand(request.getBrand());
        part.setUnit(request.getUnit());
        part.setDescription(request.getDescription());
    }
}