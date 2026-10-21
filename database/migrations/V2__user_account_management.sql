-- Apply once to existing databases before deploying this version.
-- Existing accounts remain enabled and are not forced to change passwords.
ALTER TABLE users
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
