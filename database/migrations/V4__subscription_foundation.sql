CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    monthly_price DECIMAL(12,2) NOT NULL,
    annual_price DECIMAL(12,2) NOT NULL,
    max_users INT NOT NULL,
    max_technicians INT NOT NULL,
    max_jobs_per_month INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_subscription_plans_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS organization_subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    billing_interval VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    trial_start_at DATETIME NULL,
    trial_end_at DATETIME NULL,
    current_period_start DATETIME NULL,
    current_period_end DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_org_subscription_org UNIQUE (organization_id),
    CONSTRAINT fk_org_subscription_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_org_subscription_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans (id),
    INDEX idx_org_subscription_status_end (status, trial_end_at)
);

INSERT INTO subscription_plans
    (code, name, description, monthly_price, annual_price, max_users, max_technicians, max_jobs_per_month, active, created_at)
VALUES
    ('STARTER', 'Starter', 'For small service teams getting started', 699.00, 6990.00, 3, 2, 100, TRUE, CURRENT_TIMESTAMP),
    ('BUSINESS', 'Business', 'For growing service operations', 1499.00, 14990.00, 10, 8, 500, TRUE, CURRENT_TIMESTAMP),
    ('GROWTH', 'Growth', 'For larger service teams', 2999.00, 29990.00, 25, 20, 2000, TRUE, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    monthly_price = VALUES(monthly_price),
    annual_price = VALUES(annual_price),
    max_users = VALUES(max_users),
    max_technicians = VALUES(max_technicians),
    max_jobs_per_month = VALUES(max_jobs_per_month),
    active = VALUES(active);
