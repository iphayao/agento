CREATE TABLE brand_profiles (
    id BIGSERIAL PRIMARY KEY,
    brand_name VARCHAR(255) NOT NULL,
    slogan VARCHAR(500),
    tone_of_voice TEXT,
    target_audience TEXT,
    key_messages TEXT,
    prohibited_claims TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
