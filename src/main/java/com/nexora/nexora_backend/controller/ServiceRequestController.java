package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.service_request.ServiceRequestRequest;
import com.nexora.nexora_backend.dto.service_request.ServiceRequestResponse;
import com.nexora.nexora_backend.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping
    public ResponseEntity<List<ServiceRequestResponse>> getServiceRequests() {

        return ResponseEntity.ok(
                serviceRequestService.getServiceRequests()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequestResponse> getServiceRequest(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                serviceRequestService.getServiceRequest(id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @PostMapping
    public ResponseEntity<ServiceRequestResponse> createServiceRequest(
            @Valid @RequestBody ServiceRequestRequest request
    ) {

        ServiceRequestResponse response =
                serviceRequestService.createServiceRequest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceRequestResponse> updateServiceRequest(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequestRequest request
    ) {

        return ResponseEntity.ok(
                serviceRequestService.updateServiceRequest(
                        id,
                        request
                )
        );
    }
}
