package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findAllByOrganizationIdOrderByCreatedAtDesc(
            Long organizationId
    );

    List<Notification>
    findAllByOrganizationIdAndReadFalseOrderByCreatedAtDesc(
            Long organizationId
    );

    Optional<Notification>
    findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    long countByOrganizationIdAndReadFalse(
            Long organizationId
    );
}