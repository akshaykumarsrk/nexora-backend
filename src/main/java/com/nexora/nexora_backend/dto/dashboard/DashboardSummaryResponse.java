package com.nexora.nexora_backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class DashboardSummaryResponse {

    private Long totalCustomers;

    private Long totalSites;

    private Long totalAssets;

    private Long pendingServiceRequests;

    private Long activeJobs;

    private Long completedJobs;

    private BigDecimal totalRevenue;

    private BigDecimal pendingPayments;
}