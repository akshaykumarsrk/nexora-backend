package com.nexora.nexora_backend.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    @Size(max = 150, message = "Customer name must not exceed 150 characters")
    private String name;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String phone;

    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Customer type is required")
    private String customerType;

    @Size(max = 20, message = "GST number must not exceed 20 characters")
    private String gstNumber;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}