
package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.dto.dashboard.DashboardResponse;
import com.nexora.nexora_backend.dto.dashboard.DashboardSummaryResponse;
import com.nexora.nexora_backend.dto.dashboard.JobStatusResponse;
import com.nexora.nexora_backend.dto.dashboard.RevenueResponse;
import com.nexora.nexora_backend.repository.AssetRepository;
import com.nexora.nexora_backend.repository.CustomerRepository;
import com.nexora.nexora_backend.repository.InvoiceRepository;
import com.nexora.nexora_backend.repository.JobRepository;
import com.nexora.nexora_backend.repository.ServiceRequestRepository;
import com.nexora.nexora_backend.repository.SiteRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final AssetRepository assetRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final JobRepository jobRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrganizationContext organizationContext;

    public DashboardResponse getDashboard() {

        Long organizationId = organizationContext.getOrganizationId();

        long totalCustomers =
                customerRepository.countByOrganizationId(organizationId);

        long totalSites =
                siteRepository.countByOrganizationId(organizationId);

        long totalAssets =
                assetRepository.countByOrganizationId(organizationId);

        long pendingServiceRequests =
                serviceRequestRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        "PENDING"
                );

        long activeJobs =
                jobRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        "IN_PROGRESS"
                );

        long completedJobs =
                jobRepository.countByOrganizationIdAndStatus(
                        organizationId,
                        "COMPLETED"
                );

        BigDecimal totalRevenue = moneyOrZero(
                invoiceRepository.getTotalRevenue(organizationId)
        );

        DashboardSummaryResponse summary =
                DashboardSummaryResponse.builder()
                        .totalCustomers(totalCustomers)
                        .totalSites(totalSites)
                        .totalAssets(totalAssets)
                        .pendingServiceRequests(pendingServiceRequests)
                        .activeJobs(activeJobs)
                        .completedJobs(completedJobs)
                        .totalRevenue(totalRevenue)
                        .pendingPayments(BigDecimal.ZERO.setScale(2))
                        .build();

        return DashboardResponse.builder()
                .summary(summary)
                .jobStatus(getJobStatus(organizationId))
                .revenue(getRevenue(organizationId))
                .build();
    }

    private List<JobStatusResponse> getJobStatus(Long organizationId) {

        return jobRepository.countJobsByStatus(organizationId)
                .stream()
                .map(row -> JobStatusResponse.builder()
                        .status((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build()
                )
                .toList();
    }

    private List<RevenueResponse> getRevenue(Long organizationId) {

        return invoiceRepository.getRevenueByDate(organizationId)
                .stream()
                .map(row -> {
                    LocalDate date = (LocalDate) row[0];

                    BigDecimal amount = row[1] == null
                            ? BigDecimal.ZERO.setScale(2)
                            : ((BigDecimal) row[1])
                            .setScale(2, RoundingMode.HALF_UP);

                    return RevenueResponse.builder()
                            .date(date)
                            .amount(amount)
                            .build();
                })
                .toList();
    }

    private BigDecimal moneyOrZero(BigDecimal amount) {
        return amount == null
                ? BigDecimal.ZERO.setScale(2)
                : amount.setScale(2, RoundingMode.HALF_UP);
    }
}