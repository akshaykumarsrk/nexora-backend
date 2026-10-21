package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.converter.PartConverter;
import com.nexora.nexora_backend.dto.part.PartRequest;
import com.nexora.nexora_backend.dto.part.PartResponse;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.Part;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.PartRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    public List<PartResponse> getAllParts() {

        Long organizationId =
                organizationContext.getOrganizationId();

        return partRepository
                .findAllByOrganizationId(organizationId)
                .stream()
                .map(PartConverter::partToPartResponse)
                .toList();
    }

    public PartResponse getPartById(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Part part =
                partRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Part not found"
                                )
                        );

        return PartConverter.partToPartResponse(part);
    }

    public PartResponse createPart(
            PartRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        if (partRepository
                .existsByPartCodeAndOrganizationId(
                        request.getPartCode(),
                        organizationId
                )) {

            throw new IllegalArgumentException(
                    "Part code already exists"
            );
        }

        Organization organization =
                entityManager.getReference(
                        Organization.class,
                        organizationId
                );

        Part part =
                PartConverter.partRequestToPart(request);

        part.setOrganization(organization);

        Part savedPart =
                partRepository.save(part);

        return PartConverter.partToPartResponse(savedPart);
    }

    public PartResponse updatePart(
            Long id,
            PartRequest request
    ) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Part part =
                partRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Part not found"
                                )
                        );

        if (!request.getPartCode()
                .equals(part.getPartCode())
                && partRepository
                .existsByPartCodeAndOrganizationId(
                        request.getPartCode(),
                        organizationId
                )) {

            throw new IllegalArgumentException(
                    "Part code already exists"
            );
        }

        PartConverter.updatePartFromRequest(
                part,
                request
        );

        Part updatedPart =
                partRepository.save(part);

        return PartConverter
                .partToPartResponse(updatedPart);
    }

    public void deletePart(Long id) {

        Long organizationId =
                organizationContext.getOrganizationId();

        Part part =
                partRepository
                        .findByIdAndOrganizationId(
                                id,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Part not found"
                                )
                        );

        partRepository.delete(part);
    }
}