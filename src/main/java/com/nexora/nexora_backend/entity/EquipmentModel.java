package com.nexora.nexora_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_models", uniqueConstraints = @UniqueConstraint(name = "uk_equipment_model_org_brand_number", columnNames = {"organization_id", "brand_id", "model_number"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class EquipmentModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(name = "product_type", nullable = false, length = 100)
    private String productType;

    @Column(name = "model_number", nullable = false, length = 100)
    private String modelNumber;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist void onCreate() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; if (active == null) active = true; }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }
}
