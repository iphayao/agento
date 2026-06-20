CREATE TABLE IF NOT EXISTS brand_profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand_name      VARCHAR(255) NOT NULL,
    slogan          TEXT,
    tone_of_voice   TEXT,
    target_audience TEXT,
    key_messages    TEXT,
    prohibited_claims TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
