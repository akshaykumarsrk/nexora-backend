package com.nexora.nexora_backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DashboardResponse {

    private DashboardSummaryResponse summary;

    private List<JobStatusResponse> jobStatus;

    private List<RevenueResponse> revenue;
}