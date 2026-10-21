package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.dto.catalog.BrandRequest;
import com.nexora.nexora_backend.dto.catalog.BrandResponse;
import com.nexora.nexora_backend.entity.Brand;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.BrandRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class BrandService {
    private final BrandRepository repository;
    private final com.nexora.nexora_backend.repository.EquipmentModelRepository equipmentModelRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    @Transactional(readOnly = true) public List<BrandResponse> getAll() {
        return repository.findAllByOrganizationIdOrderByNameAsc(organizationContext.getOrganizationId()).stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true) public BrandResponse getById(Long id) { return toResponse(findOwned(id)); }
    public BrandResponse create(BrandRequest request) {
        Long orgId = organizationContext.getOrganizationId(); String name = request.getName().trim();
        if (repository.existsByNameIgnoreCaseAndOrganizationId(name, orgId)) throw new IllegalArgumentException("A brand with this name already exists");
        Brand brand = Brand.builder().organization(entityManager.getReference(Organization.class, orgId)).name(name).description(clean(request.getDescription())).active(request.getActive() == null || request.getActive()).build();
        return toResponse(repository.save(brand));
    }
    public BrandResponse update(Long id, BrandRequest request) {
        Long orgId = organizationContext.getOrganizationId(); Brand brand = findOwned(id); String name = request.getName().trim();
        if (repository.existsByNameIgnoreCaseAndOrganizationIdAndIdNot(name, orgId, id)) throw new IllegalArgumentException("A brand with this name already exists");
        brand.setName(name); brand.setDescription(clean(request.getDescription())); if (request.getActive() != null) brand.setActive(request.getActive());
        return toResponse(repository.save(brand));
    }
    public void delete(Long id) { Brand brand = findOwned(id); if (equipmentModelRepository.existsByOrganizationIdAndBrandId(organizationContext.getOrganizationId(), id)) throw new IllegalArgumentException("This brand is used by equipment models. Deactivate it instead of deleting it."); repository.delete(brand); }
    private Brand findOwned(Long id) { return repository.findByIdAndOrganizationId(id, organizationContext.getOrganizationId()).orElseThrow(() -> new ResourceNotFoundException("Brand not found")); }
    private BrandResponse toResponse(Brand b) { return BrandResponse.builder().id(b.getId()).name(b.getName()).description(b.getDescription()).active(b.getActive()).createdAt(b.getCreatedAt()).updatedAt(b.getUpdatedAt()).build(); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
