package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByOrganizationIdOrderByIdDesc(Long organizationId);

    Optional<Employee> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByEmployeeCodeAndOrganizationId(String employeeCode, Long organizationId);

    boolean existsByEmailAndOrganizationId(String email, Long organizationId);

    boolean existsByEmployeeCodeAndOrganizationIdAndIdNot(
            String employeeCode, Long organizationId, Long id);

    boolean existsByEmailAndOrganizationIdAndIdNot(
            String email, Long organizationId, Long id);
}
