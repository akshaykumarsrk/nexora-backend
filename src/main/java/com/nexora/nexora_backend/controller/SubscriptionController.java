package com.nexora.nexora_backend.controller;

import com.nexora.nexora_backend.dto.subscription.OrganizationSubscriptionResponse;
import com.nexora.nexora_backend.dto.subscription.StartTrialRequest;
import com.nexora.nexora_backend.dto.subscription.SubscriptionPlanResponse;
import com.nexora.nexora_backend.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public List<SubscriptionPlanResponse> getPlans() {
        return subscriptionService.getPlans();
    }

    @GetMapping("/current")
    public ResponseEntity<OrganizationSubscriptionResponse> getCurrentSubscription() {
        OrganizationSubscriptionResponse response = subscriptionService.getCurrentSubscription();
        return response == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    @PostMapping("/trial")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationSubscriptionResponse> startTrial(
            @Valid @RequestBody StartTrialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.startTrial(request));
    }
}
