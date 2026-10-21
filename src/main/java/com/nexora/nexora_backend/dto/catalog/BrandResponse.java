package com.nexora.nexora_backend.dto.catalog;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class BrandResponse {
    private Long id; private String name; private String description; private Boolean active;
    private LocalDateTime createdAt; private LocalDateTime updatedAt;
}
