package com.nexora.nexora_backend.dto.technician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TechnicianResponse {

    private Long id;

    private String name;

    private String phone;

    private String email;

    private String employeeCode;

    private String specialization;

    private String status;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}