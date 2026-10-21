package com.nexora.nexora_backend.dto.part;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartRequest {

    @NotBlank(message = "Part code is required")
    @Size(
            max = 50,
            message = "Part code must not exceed 50 characters"
    )
    private String partCode;

    @NotBlank(message = "Part name is required")
    @Size(
            max = 150,
            message = "Part name must not exceed 150 characters"
    )
    private String name;

    @Size(
            max = 100,
            message = "Category must not exceed 100 characters"
    )
    private String category;

    @Size(
            max = 100,
            message = "Brand must not exceed 100 characters"
    )
    private String brand;

    @Size(
            max = 50,
            message = "Unit must not exceed 50 characters"
    )
    private String unit;

    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
    private String description;
}