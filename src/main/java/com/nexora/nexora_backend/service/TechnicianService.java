package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.TechnicianConverter;
import com.nexora.nexora_backend.dto.technician.TechnicianRequest;
import com.nexora.nexora_backend.dto.technician.TechnicianResponse;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Technician;
import com.nexora.nexora_backend.repository.TechnicianRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    public List<TechnicianResponse> getAllTechnicians() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return technicianRepository
                .findAllByOrganizationId(organizationId)
                .stream()
                .map(TechnicianConverter::technicianToTechnicianResponse)
                .toList();
    }

    public TechnicianResponse getTechnicianById(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Technician technician =
                technicianRepository
                        .findByIdAndOrganizationId(id, organizationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Technician not found"
                                )
                        );

        return TechnicianConverter
                .technicianToTechnicianResponse(technician);
    }

    public TechnicianResponse createTechnician(
            TechnicianRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        if (request.getEmployeeCode() != null
                && !request.getEmployeeCode().isBlank()
                && technicianRepository
                .existsByEmployeeCodeAndOrganizationId(
                        request.getEmployeeCode(),
                        organizationId
                )) {

            throw new IllegalArgumentException(
                    "Employee code already exists"
            );
        }

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );

        Technician technician =
                TechnicianConverter
                        .technicianRequestToTechnician(request);

        technician.setOrganization(organization);

        Technician savedTechnician =
                technicianRepository.save(technician);

        return TechnicianConverter
                .technicianToTechnicianResponse(savedTechnician);
    }

    public TechnicianResponse updateTechnician(
            Long id,
            TechnicianRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Technician technician =
                technicianRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Technician not found"
                                )
                        );

        if (request.getEmployeeCode() != null
                && !request.getEmployeeCode().isBlank()
                && !request.getEmployeeCode()
                .equals(technician.getEmployeeCode())
                && technicianRepository
                .existsByEmployeeCodeAndOrganizationId(
                        request.getEmployeeCode(),
                        organizationId
                )) {

            throw new IllegalArgumentException(
                    "Employee code already exists"
            );
        }

        TechnicianConverter
                .updateTechnicianFromRequest(
                        technician,
                        request
                );

        Technician updatedTechnician =
                technicianRepository.save(technician);

        return TechnicianConverter
                .technicianToTechnicianResponse(
                        updatedTechnician
                );
    }

    public void deleteTechnician(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Technician technician =
                technicianRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Technician not found"
                                )
                        );

        technicianRepository.delete(technician);
    }
}