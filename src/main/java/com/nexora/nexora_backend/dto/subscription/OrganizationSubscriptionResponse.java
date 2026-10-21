package com.nexora.nexora_backend.dto.subscription;

import com.nexora.nexora_backend.entity.OrganizationSubscription;

import java.time.LocalDateTime;

public record OrganizationSubscriptionResponse(
        Long id,
        String planCode,
        String planName,
        String status,
        String billingInterval,
        LocalDateTime trialStartAt,
        LocalDateTime trialEndAt,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd
) {
    public static OrganizationSubscriptionResponse from(OrganizationSubscription subscription) {
        return new OrganizationSubscriptionResponse(
                subscription.getId(),
                subscription.getPlan().getCode(),
                subscription.getPlan().getName(),
                subscription.getStatus().name(),
                subscription.getBillingInterval(),
                subscription.getTrialStartAt(),
                subscription.getTrialEndAt(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd()
        );
    }
}
