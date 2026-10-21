package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findAllByOrganizationIdOrderByNameAsc(Long organizationId);
    Optional<Brand> findByIdAndOrganizationId(Long id, Long organizationId);
    boolean existsByNameIgnoreCaseAndOrganizationId(String name, Long organizationId);
    boolean existsByNameIgnoreCaseAndOrganizationIdAndIdNot(String name, Long organizationId, Long id);
}
