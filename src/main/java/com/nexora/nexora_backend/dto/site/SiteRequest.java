package com.nexora.nexora_backend.dto.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Site name is required")
    @Size(max = 150, message = "Site name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private String city;

    private String state;

    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "Pincode must contain exactly 6 digits"
    )
    private String pincode;

    private String contactPerson;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Contact phone must contain exactly 10 digits"
    )
    private String contactPhone;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
