package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.ai.AiResponse;
import com.nexora.nexora_backend.dto.ai.TechnicianAssistantRequest;
import com.nexora.nexora_backend.service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController {
    private final AiAssistantService aiAssistantService;

    @GetMapping("/service-requests/{id}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<AiResponse> summarizeServiceRequest(@PathVariable Long id) {
        return ResponseEntity.ok(aiAssistantService.summarizeServiceRequest(id));
    }

    @PostMapping("/technician-assistant")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    public ResponseEntity<AiResponse> technicianAssistant(
            @Valid @RequestBody TechnicianAssistantRequest request) {
        return ResponseEntity.ok(aiAssistantService.assistTechnician(request));
    }
}
