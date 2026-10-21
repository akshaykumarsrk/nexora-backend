-- NEXORA employee invitations. Back up production before applying.
-- Run once against the target database.
CREATE TABLE user_invitations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    organization_id BIGINT NOT NULL,
    technician_id BIGINT NULL,
    email VARCHAR(254) NOT NULL,
    username VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    accepted_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_invitation_token_hash UNIQUE (token_hash),
    INDEX idx_invitation_expiry (expires_at),
    INDEX idx_invitation_email_expiry (email, expires_at),
    INDEX idx_invitation_username_expiry (username, expires_at),
    INDEX idx_invitation_technician_expiry (technician_id, expires_at),
    CONSTRAINT fk_user_invitation_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_user_invitation_technician
        FOREIGN KEY (technician_id) REFERENCES technicians(id)
) ENGINE=InnoDB;
