package com.nexora.nexora_backend.dto.technician;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TechnicianRequest {

    @NotBlank(message = "Technician name is required")
    @Size(
            max = 150,
            message = "Technician name must not exceed 150 characters"
    )
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @Email(message = "Invalid email address")
    private String email;

    @Size(
            max = 50,
            message = "Employee code must not exceed 50 characters"
    )
    private String employeeCode;

    private String specialization;

    @NotBlank(message = "Technician status is required")
    private String status;

    @Size(
            max = 1000,
            message = "Notes must not exceed 1000 characters"
    )
    private String notes;
}