package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.security.OrganizationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings/ai-prompt")
@RequiredArgsConstructor
public class AiPromptSettingsController {
    private final EntityManager entityManager;
    private final OrganizationContext organizationContext;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PromptSettings> get() {
        Organization organization = organization();
        return ResponseEntity.ok(new PromptSettings(organization.getAiAssistantCustomInstructions()));
    }

    @PutMapping
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PromptSettings> update(@RequestBody PromptSettings request) {
        String instructions = request == null ? null : request.getInstructions();
        if (instructions != null && instructions.length() > 2000) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Instructions must be 2000 characters or fewer");
        }
        Organization organization = organization();
        organization.setAiAssistantCustomInstructions(instructions == null || instructions.isBlank() ? null : instructions.trim());
        entityManager.merge(organization);
        return ResponseEntity.ok(new PromptSettings(organization.getAiAssistantCustomInstructions()));
    }

    private Organization organization() {
        Organization value = entityManager.find(Organization.class, organizationContext.getOrganizationId());
        if (value == null) throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.NOT_FOUND, "Organization not found");
        return value;
    }

    @Getter @Setter
    public static class PromptSettings {
        @Size(max = 2000) private String instructions;
        public PromptSettings() {}
        public PromptSettings(String instructions) { this.instructions = instructions; }
    }
}
