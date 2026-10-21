package com.nexora.nexora_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans", uniqueConstraints = {
        @UniqueConstraint(name = "uk_subscription_plans_code", columnNames = "code")
})
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "monthly_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "annual_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal annualPrice;

    @Column(name = "max_users", nullable = false)
    private int maxUsers;

    @Column(name = "max_technicians", nullable = false)
    private int maxTechnicians;

    @Column(name = "max_jobs_per_month", nullable = false)
    private int maxJobsPerMonth;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
