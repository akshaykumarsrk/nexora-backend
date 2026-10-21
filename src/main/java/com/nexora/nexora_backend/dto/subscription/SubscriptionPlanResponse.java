package com.nexora.nexora_backend.dto.subscription;

import com.nexora.nexora_backend.entity.SubscriptionPlan;

import java.math.BigDecimal;

public record SubscriptionPlanResponse(
        String code,
        String name,
        String description,
        BigDecimal monthlyPrice,
        BigDecimal annualPrice,
        int maxUsers,
        int maxTechnicians,
        int maxJobsPerMonth
) {
    public static SubscriptionPlanResponse from(SubscriptionPlan plan) {
        return new SubscriptionPlanResponse(
                plan.getCode(), plan.getName(), plan.getDescription(),
                plan.getMonthlyPrice(), plan.getAnnualPrice(),
                plan.getMaxUsers(), plan.getMaxTechnicians(), plan.getMaxJobsPerMonth()
        );
    }
}
