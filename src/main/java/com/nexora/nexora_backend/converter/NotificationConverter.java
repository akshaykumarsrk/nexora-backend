package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.notification.NotificationRequest;
import com.nexora.nexora_backend.dto.notification.NotificationResponse;
import com.nexora.nexora_backend.entity.Notification;
import com.nexora.nexora_backend.entity.Organization;

public class NotificationConverter {

    public static Notification
    notificationRequestToNotification(
            NotificationRequest request,
            Organization organization
    ) {

        return Notification.builder()
                .organization(organization)
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .referenceType(
                        request.getReferenceType()
                )
                .referenceId(
                        request.getReferenceId()
                )
                .read(false)
                .build();
    }


    public static NotificationResponse
    notificationToNotificationResponse(
            Notification notification
    ) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceType(
                        notification.getReferenceType()
                )
                .referenceId(
                        notification.getReferenceId()
                )
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}