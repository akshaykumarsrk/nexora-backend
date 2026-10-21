package com.nexora.nexora_backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JobStatusResponse {

    private String status;

    private Long count;
}