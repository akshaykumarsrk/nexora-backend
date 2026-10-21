package com.nexora.nexora_backend.converter;

import com.nexora.nexora_backend.dto.technician.TechnicianRequest;
import com.nexora.nexora_backend.dto.technician.TechnicianResponse;
import com.nexora.nexora_backend.entity.Technician;

public class TechnicianConverter {

    public static Technician technicianRequestToTechnician(
            TechnicianRequest request
    ) {

        return Technician.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .employeeCode(request.getEmployeeCode())
                .specialization(request.getSpecialization())
                .status(request.getStatus())
                .notes(request.getNotes())
                .build();
    }

    public static TechnicianResponse technicianToTechnicianResponse(
            Technician technician
    ) {

        return TechnicianResponse.builder()
                .id(technician.getId())
                .name(technician.getName())
                .phone(technician.getPhone())
                .email(technician.getEmail())
                .employeeCode(technician.getEmployeeCode())
                .specialization(technician.getSpecialization())
                .status(technician.getStatus())
                .notes(technician.getNotes())
                .createdAt(technician.getCreatedAt())
                .updatedAt(technician.getUpdatedAt())
                .build();
    }

    public static void updateTechnicianFromRequest(
            Technician technician,
            TechnicianRequest request
    ) {

        technician.setName(request.getName());
        technician.setPhone(request.getPhone());
        technician.setEmail(request.getEmail());
        technician.setEmployeeCode(request.getEmployeeCode());
        technician.setSpecialization(request.getSpecialization());
        technician.setStatus(request.getStatus());
        technician.setNotes(request.getNotes());
    }
}