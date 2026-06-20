CREATE TABLE IF NOT EXISTS campaigns (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    objective       TEXT,
    channel         VARCHAR(100),
    target_audience TEXT,
    content_angle   TEXT,
    status          VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
