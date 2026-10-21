package com.nexora.nexora_backend.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationRequest {

    @NotBlank(message = "Title is required")
    @Size(
            max = 150,
            message = "Title must not exceed 150 characters"
    )
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Notification type is required")
    @Size(
            max = 50,
            message = "Notification type must not exceed 50 characters"
    )
    private String type;

    private String referenceType;

    private Long referenceId;
}