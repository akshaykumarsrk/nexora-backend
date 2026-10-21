package com.nexora.nexora_backend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private String type;

    private String referenceType;

    private Long referenceId;

    private Boolean read;

    private LocalDateTime createdAt;
}