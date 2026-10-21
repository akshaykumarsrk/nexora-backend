package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findAllByActiveTrueOrderByMonthlyPriceAsc();
    Optional<SubscriptionPlan> findByCodeAndActiveTrue(String code);
}
