package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.dto.catalog.EquipmentModelRequest;
import com.nexora.nexora_backend.dto.catalog.EquipmentModelResponse;
import com.nexora.nexora_backend.entity.Brand;
import com.nexora.nexora_backend.entity.EquipmentModel;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.exception.ResourceNotFoundException;
import com.nexora.nexora_backend.repository.BrandRepository;
import com.nexora.nexora_backend.repository.EquipmentModelRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class EquipmentModelService {
    private final EquipmentModelRepository repository;
    private final BrandRepository brandRepository;
    private final OrganizationContext organizationContext;
    private final EntityManager entityManager;

    @Transactional(readOnly = true) public List<EquipmentModelResponse> getAll(Long brandId) {
        Long orgId = organizationContext.getOrganizationId();
        if (brandId != null) requireBrand(brandId);
        List<EquipmentModel> models = brandId == null ? repository.findAllByOrganizationIdOrderByProductTypeAscModelNumberAsc(orgId) : repository.findAllByOrganizationIdAndBrandIdOrderByModelNumberAsc(orgId, brandId);
        return models.stream().map(this::toResponse).toList();
    }
    @Transactional(readOnly = true) public EquipmentModelResponse getById(Long id) { return toResponse(findOwned(id)); }
    public EquipmentModelResponse create(EquipmentModelRequest request) {
        Long orgId = organizationContext.getOrganizationId(); Brand brand = requireBrand(request.getBrandId()); String number = request.getModelNumber().trim();
        if (repository.existsByOrganizationIdAndBrandIdAndModelNumberIgnoreCase(orgId, brand.getId(), number)) throw new IllegalArgumentException("This model number already exists for the selected brand");
        EquipmentModel model = EquipmentModel.builder().organization(entityManager.getReference(Organization.class, orgId)).brand(brand).productType(request.getProductType().trim()).modelNumber(number).description(clean(request.getDescription())).active(request.getActive() == null || request.getActive()).build();
        return toResponse(repository.save(model));
    }
    public EquipmentModelResponse update(Long id, EquipmentModelRequest request) {
        Long orgId = organizationContext.getOrganizationId(); EquipmentModel model = findOwned(id); Brand brand = requireBrand(request.getBrandId()); String number = request.getModelNumber().trim();
        if (repository.existsByOrganizationIdAndBrandIdAndModelNumberIgnoreCaseAndIdNot(orgId, brand.getId(), number, id)) throw new IllegalArgumentException("This model number already exists for the selected brand");
        model.setBrand(brand); model.setProductType(request.getProductType().trim()); model.setModelNumber(number); model.setDescription(clean(request.getDescription())); if (request.getActive() != null) model.setActive(request.getActive());
        return toResponse(repository.save(model));
    }
    public void delete(Long id) { repository.delete(findOwned(id)); }
    private Brand requireBrand(Long id) { return brandRepository.findByIdAndOrganizationId(id, organizationContext.getOrganizationId()).orElseThrow(() -> new ResourceNotFoundException("Brand not found")); }
    private EquipmentModel findOwned(Long id) { return repository.findByIdAndOrganizationId(id, organizationContext.getOrganizationId()).orElseThrow(() -> new ResourceNotFoundException("Equipment model not found")); }
    private EquipmentModelResponse toResponse(EquipmentModel m) { return EquipmentModelResponse.builder().id(m.getId()).brandId(m.getBrand().getId()).brandName(m.getBrand().getName()).productType(m.getProductType()).modelNumber(m.getModelNumber()).description(m.getDescription()).active(m.getActive()).createdAt(m.getCreatedAt()).updatedAt(m.getUpdatedAt()).build(); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
