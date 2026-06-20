CREATE TABLE IF NOT EXISTS generated_content (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id      UUID NOT NULL,
    content_type     VARCHAR(100),
    channel          VARCHAR(100),
    title            TEXT,
    body             TEXT,
    hook             TEXT,
    call_to_action   TEXT,
    hashtags         TEXT,
    status           VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    ai_model         VARCHAR(255),
    prompt_version   VARCHAR(50),
    compliance_notes TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_generated_content_campaign_id
    ON generated_content (campaign_id);

CREATE INDEX IF NOT EXISTS idx_generated_content_status
    ON generated_content (status);
