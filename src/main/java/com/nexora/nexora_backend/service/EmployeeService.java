package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.EmployeeConverter;
import com.nexora.nexora_backend.dto.employee.EmployeeRequest;
import com.nexora.nexora_backend.entity.Employee;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.exception.BadRequestException;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.EmployeeRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        Long organizationId = organizationContext.getOrganizationId();
        return employeeRepository.findAllByOrganizationIdOrderByIdDesc(organizationId);
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        Long organizationId = organizationContext.getOrganizationId();
        return employeeRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    public Employee createEmployee(EmployeeRequest request) {
        Long organizationId = organizationContext.getOrganizationId();

        if (employeeRepository.existsByEmployeeCodeAndOrganizationId(
                request.getEmployeeCode(), organizationId)) {
            throw new BadRequestException("Employee code already exists in this organization");
        }

        if (employeeRepository.existsByEmailAndOrganizationId(
                request.getEmail(), organizationId)) {
            throw new BadRequestException("Email already exists in this organization");
        }

        Employee employee = EmployeeConverter.EmployeeRequestToEmployee(request);
        employee.setOrganization(entityManager.getReference(Organization.class, organizationId));
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, EmployeeRequest request) {
        Long organizationId = organizationContext.getOrganizationId();
        Employee existingEmployee = employeeRepository
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employeeRepository.existsByEmployeeCodeAndOrganizationIdAndIdNot(
                request.getEmployeeCode(), organizationId, id)) {
            throw new BadRequestException("Employee code already exists in this organization");
        }

        if (employeeRepository.existsByEmailAndOrganizationIdAndIdNot(
                request.getEmail(), organizationId, id)) {
            throw new BadRequestException("Email already exists in this organization");
        }

        existingEmployee.setEmployeeCode(request.getEmployeeCode());
        existingEmployee.setName(request.getName());
        existingEmployee.setEmail(request.getEmail());
        existingEmployee.setDepartment(request.getDepartment());
        existingEmployee.setDesignation(request.getDesignation());
        existingEmployee.setJoiningDate(request.getJoiningDate());
        existingEmployee.setStatus(request.getStatus());

        return employeeRepository.save(existingEmployee);
    }

    public void deleteEmployee(Long id) {
        Long organizationId = organizationContext.getOrganizationId();
        Employee employee = employeeRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        employeeRepository.delete(employee);
    }
}
