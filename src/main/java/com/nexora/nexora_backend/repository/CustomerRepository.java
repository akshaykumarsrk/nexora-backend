package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // means: Give me only customers belonging to Organization #1.
    List<Customer> findAllByOrganizationId(Long organizationId);

    Optional<Customer> findByIdAndOrganizationId(
            Long id,
            Long organizationId
    );

    long countByOrganizationId(Long organizationId);
}