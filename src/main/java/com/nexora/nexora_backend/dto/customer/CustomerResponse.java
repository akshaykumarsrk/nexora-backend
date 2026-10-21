package com.nexora.nexora_backend.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String customerType;
    private String gstNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}