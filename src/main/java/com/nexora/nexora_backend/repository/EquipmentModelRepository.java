package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.EquipmentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EquipmentModelRepository extends JpaRepository<EquipmentModel, Long> {
    List<EquipmentModel> findAllByOrganizationIdOrderByProductTypeAscModelNumberAsc(Long organizationId);
    List<EquipmentModel> findAllByOrganizationIdAndBrandIdOrderByModelNumberAsc(Long organizationId, Long brandId);
    Optional<EquipmentModel> findByIdAndOrganizationId(Long id, Long organizationId);
    boolean existsByOrganizationIdAndBrandId(Long organizationId, Long brandId);
    boolean existsByOrganizationIdAndBrandIdAndModelNumberIgnoreCase(Long organizationId, Long brandId, String modelNumber);
    boolean existsByOrganizationIdAndBrandIdAndModelNumberIgnoreCaseAndIdNot(Long organizationId, Long brandId, String modelNumber, Long id);
}
