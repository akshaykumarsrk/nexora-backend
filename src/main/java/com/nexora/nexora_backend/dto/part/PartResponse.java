package com.nexora.nexora_backend.dto.part;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PartResponse {

    private Long id;

    private String partCode;

    private String name;

    private String category;

    private String brand;

    private String unit;

    private Boolean active;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}