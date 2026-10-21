package com.nexora.nexora_backend.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AiResponse {
    private String feature;
    private String result;
    private String model;
}
