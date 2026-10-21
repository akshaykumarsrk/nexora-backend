package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.dashboard.DashboardResponse;
import com.nexora.nexora_backend.service.DashboardService;
import com.nexora.nexora_backend.repository.JobRepository;
import com.nexora.nexora_backend.repository.TechnicianRepository;
import com.nexora.nexora_backend.repository.InvoiceRepository;
import com.nexora.nexora_backend.entity.Job;
import com.nexora.nexora_backend.entity.Technician;
import com.nexora.nexora_backend.security.OrganizationContext;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final JobRepository jobRepository;
    private final TechnicianRepository technicianRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrganizationContext organizationContext;


    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {

        return ResponseEntity.ok(dashboardService.getDashboard());
    }
    @PreAuthorize("hasRole('TECHNICIAN')")
    @GetMapping("/technician")
    public ResponseEntity<Map<String, Object>> getTechnicianDashboard(Authentication authentication) {
        Long organizationId = organizationContext.getOrganizationId();
        String username = authentication.getName();
        Technician technician = technicianRepository.findByUserUsernameAndOrganizationId(username, organizationId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Your login is not linked to a technician profile. Ask your administrator to link your account."));
        List<Job> jobs = jobRepository.findAllByOrganizationIdAndTechnician_User_Username(organizationId, username);
        long completed = jobs.stream().filter(j -> "COMPLETED".equalsIgnoreCase(j.getStatus())).count();
        long active = jobs.stream().filter(j -> "IN_PROGRESS".equalsIgnoreCase(j.getStatus())).count();
        long scheduled = jobs.stream().filter(j -> "SCHEDULED".equalsIgnoreCase(j.getStatus()) || "PENDING".equalsIgnoreCase(j.getStatus())).count();
        BigDecimal billed = jobs.stream().filter(j -> "COMPLETED".equalsIgnoreCase(j.getStatus()))
                .map(j -> invoiceRepository.findByJobIdAndOrganizationId(j.getId(), organizationId)
                        .map(i -> i.getTotalAmount() == null ? BigDecimal.ZERO : i.getTotalAmount())
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Map<String, Object>> jobRows = jobs.stream().map(j -> Map.<String, Object>ofEntries(
                Map.entry("id", j.getId()), Map.entry("jobNumber", j.getJobNumber()),
                Map.entry("title", j.getServiceRequest() == null ? j.getJobNumber() : j.getServiceRequest().getTitle()),
                Map.entry("customerName", j.getCustomer() == null ? "Customer" : j.getCustomer().getName()),
                Map.entry("siteName", j.getSite() == null ? "Site" : j.getSite().getName()),
                Map.entry("status", j.getStatus() == null ? "UNKNOWN" : j.getStatus()),
                Map.entry("priority", j.getServiceRequest() == null || j.getServiceRequest().getPriority() == null ? "NORMAL" : j.getServiceRequest().getPriority()),
                Map.entry("scheduledAt", j.getScheduledAt() == null ? "" : j.getScheduledAt().toString()),
                Map.entry("completedAt", j.getCompletedAt() == null ? "" : j.getCompletedAt().toString())
        )).toList();
        return ResponseEntity.ok(Map.of("technicianName", technician.getName(), "totalAssignedJobs", jobs.size(),
                "activeJobs", active, "completedJobs", completed, "scheduledJobs", scheduled,
                "completedJobBilledValue", billed, "jobs", jobRows));
    }

}
