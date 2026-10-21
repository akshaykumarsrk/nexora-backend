package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
