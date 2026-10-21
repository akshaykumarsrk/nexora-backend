package com.nexora.nexora_backend.dto.site;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class SiteResponse {

    private Long id;

    private Long customerId;

    private String name;

    private String address;

    private String city;

    private String state;

    private String pincode;

    private String contactPerson;

    private String contactPhone;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
