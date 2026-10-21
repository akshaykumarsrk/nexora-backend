package com.nexora.nexora_backend.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BrandRequest {
    @NotBlank @Size(max = 100) private String name;
    @Size(max = 500) private String description;
    private Boolean active;
}
