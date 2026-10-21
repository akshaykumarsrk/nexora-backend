package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.job.CompleteJobRequest;
import com.nexora.nexora_backend.dto.job.JobRequest;
import com.nexora.nexora_backend.dto.job.JobResponse;
import com.nexora.nexora_backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(
                jobService.getAllJobs()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request
    ) {
        return ResponseEntity.ok(
                jobService.createJob(request)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequest request
    ) {
        return ResponseEntity.ok(
                jobService.updateJob(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<JobResponse> assignTechnician(
            @PathVariable Long id,
            @RequestParam Long technicianId
    ) {
        return ResponseEntity.ok(
                jobService.assignTechnician(id, technicianId)
        );
    }

    @PatchMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<JobResponse> scheduleJob(@PathVariable Long id) {
        return ResponseEntity.ok(
                jobService.scheduleJob(id)
        );
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<JobResponse> startJob(@PathVariable Long id) {
        return ResponseEntity.ok(
                jobService.startJob(id)
        );
    }

    @PatchMapping("/{id}/hold")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<JobResponse> holdJob(@PathVariable Long id) {
        return ResponseEntity.ok(
                jobService.holdJob(id)
        );
    }

    @PatchMapping("/{id}/resume")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<JobResponse> resumeJob(@PathVariable Long id) {
        return ResponseEntity.ok(
                jobService.resumeJob(id)
        );
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN')")
    public ResponseEntity<JobResponse> completeJob(
            @PathVariable Long id,
            @Valid @RequestBody CompleteJobRequest request
    ) {
        return ResponseEntity.ok(
                jobService.completeJob(
                id,
                request.getWorkSummary()
        )
        );
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<JobResponse> cancelJob(@PathVariable Long id) {
        return ResponseEntity.ok(
                jobService.cancelJob(id)
        );
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<List<JobResponse>> getMyJobs() {

        return ResponseEntity.ok(jobService.getMyJobs());
    }
}
