package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnicianRepository
        extends JpaRepository<Technician, Long> {

    List<Technician> findAllByOrganizationId(
            Long organizationId
    );

    Optional<Technician> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    boolean existsByEmployeeCodeAndOrganizationId(
            String employeeCode,
            Long organizationId
    );

    Optional<Technician> findByUserUsernameAndOrganizationId(
            String username,
            Long organizationId
    );
}