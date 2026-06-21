-- Phase 7: users table, audit log, bootstrap admin user

CREATE TABLE app_users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'VIEWER',
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Bootstrap admin user (password: admin — CHANGE BEFORE PRODUCTION)
-- bcrypt of 'admin' with cost 10
INSERT INTO app_users (username, password, role)
VALUES ('admin', '$2a$10$otdpfLRByhDYRzjooT/oP.d.n9PcdnJvmazJNcOu5/HF6WV65Vb2e', 'ADMIN');

CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action      VARCHAR(60)  NOT NULL,
    entity_type VARCHAR(60),
    entity_id   UUID,
    username    VARCHAR(100),
    details     TEXT,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_username    ON audit_logs (username);
CREATE INDEX idx_audit_logs_action      ON audit_logs (action);
CREATE INDEX idx_audit_logs_entity      ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at  ON audit_logs (created_at DESC);
