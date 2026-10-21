package com.nexora.nexora_backend.controller;

import org.springframework.http.ResponseEntity;
import com.nexora.nexora_backend.dto.notification.NotificationRequest;
import com.nexora.nexora_backend.dto.notification.NotificationResponse;
import com.nexora.nexora_backend.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {

        return ResponseEntity.ok(
                notificationService
                .getAllNotifications()
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {

        return ResponseEntity.ok(
                notificationService
                .getUnreadNotifications()
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {

        return ResponseEntity.ok(
                notificationService
                .getUnreadCount()
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                notificationService
                .getNotificationById(id)
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationRequest request
    ) {

        return ResponseEntity.ok(
                notificationService
                .createNotification(request)
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                notificationService
                .markAsRead(id)
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {

        notificationService
                .markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
