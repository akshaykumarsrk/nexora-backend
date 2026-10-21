package com.nexora.nexora_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    private String phone;

    private String address;

    @Column(name = "logo_storage_key", length = 500)
    private String logoStorageKey;

    @Column(name = "use_company_logo", nullable = false)
    private boolean useCompanyLogo = true;

    @Column(name = "show_nexora_attribution", nullable = false)
    private boolean showNexoraAttribution = true;

    @Column(name = "invoice_footer_text", length = 500)
    private String invoiceFooterText;

    @Column(name = "receipt_footer_text", length = 500)
    private String receiptFooterText;

    @Column(name = "ai_assistant_custom_instructions", columnDefinition = "TEXT")
    private String aiAssistantCustomInstructions;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // @PrePersist is a JPA lifecycle annotation.
    // It means: "Run this method immediately before this entity is inserted into the database for the first time."
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
