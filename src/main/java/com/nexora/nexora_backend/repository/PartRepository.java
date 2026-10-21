package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartRepository
        extends JpaRepository<Part, Long> {

    List<Part> findAllByOrganizationId(
            Long organizationId
    );

    Optional<Part> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    boolean existsByPartCodeAndOrganizationId(
            String partCode,
            Long organizationId
    );
}