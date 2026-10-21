package com.nexora.nexora_backend.controller;

import org.springframework.http.ResponseEntity;
import com.nexora.nexora_backend.dto.job_part.JobPartRequest;
import com.nexora.nexora_backend.dto.job_part.JobPartResponse;
import com.nexora.nexora_backend.service.JobPartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-parts")
@RequiredArgsConstructor
public class JobPartController {

    private final JobPartService jobPartService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<JobPartResponse>> getAllJobParts() {

        return ResponseEntity.ok(
                jobPartService.getAllJobParts()
        );
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<JobPartResponse> getJobPartById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                jobPartService.getJobPartById(id)
        );
    }


    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<JobPartResponse>> getJobPartsByJob(
            @PathVariable Long jobId
    ) {

        return ResponseEntity.ok(
                jobPartService.getJobPartsByJob(
                jobId
        )
        );
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<JobPartResponse> addPartToJob(
            @Valid @RequestBody JobPartRequest request
    ) {

        return ResponseEntity.ok(
                jobPartService.addPartToJob(
                request
        )
        );
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJobPart(
            @PathVariable Long id
    ) {

        jobPartService.deleteJobPart(id);
        return ResponseEntity.noContent().build();
    }
}
