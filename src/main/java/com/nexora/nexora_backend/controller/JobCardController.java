package com.nexora.nexora_backend.controller;

import org.springframework.http.ResponseEntity;
import com.nexora.nexora_backend.dto.jobcard.JobCardRequest;
import com.nexora.nexora_backend.dto.jobcard.JobCardResponse;
import com.nexora.nexora_backend.service.JobCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-cards")
@RequiredArgsConstructor
public class JobCardController {

    private final JobCardService jobCardService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping
    public ResponseEntity<List<JobCardResponse>> getAllJobCards() {

        return ResponseEntity.ok(
                jobCardService.getAllJobCards()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<JobCardResponse> getJobCardById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                jobCardService.getJobCardById(id)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF', 'TECHNICIAN')")
    @GetMapping("/job/{jobId}")
    public ResponseEntity<JobCardResponse> getJobCardByJobId(
            @PathVariable Long jobId
    ) {

        return ResponseEntity.ok(
                jobCardService.getJobCardByJobId(jobId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @PostMapping
    public ResponseEntity<JobCardResponse> createJobCard(
            @Valid @RequestBody JobCardRequest request
    ) {

        return ResponseEntity.ok(
                jobCardService.createJobCard(request)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<JobCardResponse> updateJobCard(
            @PathVariable Long id,
            @Valid @RequestBody JobCardRequest request
    ) {

        return ResponseEntity.ok(
                jobCardService.updateJobCard(
                id,
                request
        )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<JobCardResponse> acknowledgeJobCard(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                jobCardService.acknowledgeJobCard(id)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJobCard(
            @PathVariable Long id
    ) {

        jobCardService.deleteJobCard(id);
        return ResponseEntity.noContent().build();
    }
}
