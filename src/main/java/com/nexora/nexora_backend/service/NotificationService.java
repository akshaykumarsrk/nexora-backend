package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.NotificationConverter;
import com.nexora.nexora_backend.dto.notification.NotificationRequest;
import com.nexora.nexora_backend.dto.notification.NotificationResponse;
import com.nexora.nexora_backend.entity.Notification;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.NotificationRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final OrganizationContext organizationContext;

    private final EntityManager entityManager;


    public List<NotificationResponse> getAllNotifications() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return notificationRepository
                .findAllByOrganizationIdOrderByCreatedAtDesc(
                        organizationId
                )
                .stream()
                .map(
                        NotificationConverter::
                                notificationToNotificationResponse
                )
                .toList();
    }


    public List<NotificationResponse> getUnreadNotifications() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return notificationRepository
                .findAllByOrganizationIdAndReadFalseOrderByCreatedAtDesc(
                        organizationId
                )
                .stream()
                .map(
                        NotificationConverter::
                                notificationToNotificationResponse
                )
                .toList();
    }


    public long getUnreadCount() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return notificationRepository
                .countByOrganizationIdAndReadFalse(
                        organizationId
                );
    }


    public NotificationResponse getNotificationById(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Notification notification =
                notificationRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found"
                                )
                        );

        return NotificationConverter
                .notificationToNotificationResponse(
                        notification
                );
    }


    // =========================================================
    // CREATE JOB NOTIFICATION
    // =========================================================

    @Transactional
    public NotificationResponse createJobNotification(
            String title,
            String message,
            String type,
            Long jobId
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );

        Notification notification =
                Notification.builder()
                        .organization(organization)
                        .title(title)
                        .message(message)
                        .type(type)
                        .referenceType("JOB")
                        .referenceId(jobId)
                        .read(false)
                        .build();

        Notification savedNotification =
                notificationRepository.save(notification);

        return NotificationConverter
                .notificationToNotificationResponse(
                        savedNotification
                );
    }


    @Transactional
    public NotificationResponse markAsRead(
            Long id
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Notification notification =
                notificationRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found"
                                )
                        );

        notification.setRead(true);

        return NotificationConverter
                .notificationToNotificationResponse(
                        notification
                );
    }


    @Transactional
    public void markAllAsRead() {

        Long organizationId =
                organizationContext.getOrganizationId();

        List<Notification> notifications =
                notificationRepository
                        .findAllByOrganizationIdAndReadFalseOrderByCreatedAtDesc(
                                organizationId
                        );

        notifications.forEach(
                notification ->
                        notification.setRead(true)
        );

        notificationRepository.saveAll(
                notifications
        );
    }

    // =========================================================
// CREATE GENERIC NOTIFICATION
// =========================================================

    @Transactional
    public NotificationResponse createNotification(
            NotificationRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );

        Notification notification =
                NotificationConverter
                        .notificationRequestToNotification(
                                request,
                                organization
                        );

        Notification savedNotification =
                notificationRepository.save(
                        notification
                );

        return NotificationConverter
                .notificationToNotificationResponse(
                        savedNotification
                );
    }
}