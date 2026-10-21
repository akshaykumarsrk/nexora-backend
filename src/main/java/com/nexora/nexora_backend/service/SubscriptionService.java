package com.nexora.nexora_backend.service;

import com.nexora.nexora_backend.dto.subscription.OrganizationSubscriptionResponse;
import com.nexora.nexora_backend.dto.subscription.SubscriptionPlanResponse;
import com.nexora.nexora_backend.dto.subscription.StartTrialRequest;
import com.nexora.nexora_backend.entity.Organization;
import com.nexora.nexora_backend.entity.OrganizationSubscription;
import com.nexora.nexora_backend.entity.SubscriptionPlan;
import com.nexora.nexora_backend.entity.SubscriptionStatus;
import com.nexora.nexora_backend.repository.OrganizationSubscriptionLockRepository;
import com.nexora.nexora_backend.repository.OrganizationSubscriptionRepository;
import com.nexora.nexora_backend.repository.SubscriptionPlanRepository;
import com.nexora.nexora_backend.security.OrganizationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final int TRIAL_DAYS = 30;

    private final SubscriptionPlanRepository planRepository;
    private final OrganizationSubscriptionRepository subscriptionRepository;
    private final OrganizationSubscriptionLockRepository organizationLockRepository;
    private final OrganizationContext organizationContext;

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getPlans() {
        return planRepository.findAllByActiveTrueOrderByMonthlyPriceAsc()
                .stream().map(SubscriptionPlanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrganizationSubscriptionResponse getCurrentSubscription() {
        Long organizationId = requireOrganizationId();
        return subscriptionRepository.findByOrganizationId(organizationId)
                .map(OrganizationSubscriptionResponse::from)
                .orElse(null);
    }

    @Transactional
    public OrganizationSubscriptionResponse startTrial(StartTrialRequest request) {
        Long organizationId = requireOrganizationId();

        // Serialize trial creation per organization, then enforce the unique subscription record.
        Organization organization = organizationLockRepository
                .findByIdForSubscriptionUpdate(organizationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organization not found"));

        if (subscriptionRepository.existsByOrganizationId(organizationId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This organization already has a subscription or has used its trial");
        }

        String planCode = request.planCode().trim().toUpperCase(Locale.ROOT);
        SubscriptionPlan plan = planRepository.findByCodeAndActiveTrue(planCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "The selected plan is not available"));

        LocalDateTime now = LocalDateTime.now();
        OrganizationSubscription subscription = new OrganizationSubscription();
        subscription.setOrganization(organization);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.TRIALING);
        subscription.setBillingInterval("MONTHLY");
        subscription.setTrialStartAt(now);
        subscription.setTrialEndAt(now.plusDays(TRIAL_DAYS));
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plusDays(TRIAL_DAYS));

        return OrganizationSubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    private Long requireOrganizationId() {
        Long id = organizationContext.getOrganizationId();
        if (id == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "An organization account is required");
        }
        return id;
    }
}
