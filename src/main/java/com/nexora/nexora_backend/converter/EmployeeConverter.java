package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.employee.EmployeeRequest;
import com.nexora.nexora_backend.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeConverter {

    public static Employee EmployeeRequestToEmployee(EmployeeRequest employeeRequest) {
        return Employee.builder()
                .employeeCode(employeeRequest.getEmployeeCode())
                .name(employeeRequest.getName())
                .email(employeeRequest.getEmail())
                .department(employeeRequest.getDepartment())
                .designation(employeeRequest.getDesignation())
                .joiningDate(employeeRequest.getJoiningDate())
                .status(employeeRequest.getStatus())
                .build();
    }
}
