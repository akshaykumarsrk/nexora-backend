package com.nexora.nexora_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiError {

    private int status;

    private String message;

    private Map<String, String> errors;
}
