-- Phase 4: Performance Learning tables

CREATE TABLE content_performance (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    generated_content_id UUID        NOT NULL REFERENCES generated_content(id) ON DELETE CASCADE,
    channel             VARCHAR(50)  NOT NULL,
    published_at        TIMESTAMP,
    impressions         BIGINT       NOT NULL DEFAULT 0,
    views               BIGINT       NOT NULL DEFAULT 0,
    clicks              BIGINT       NOT NULL DEFAULT 0,
    likes               BIGINT       NOT NULL DEFAULT 0,
    comments            BIGINT       NOT NULL DEFAULT 0,
    shares              BIGINT       NOT NULL DEFAULT 0,
    orders              BIGINT       NOT NULL DEFAULT 0,
    revenue             NUMERIC(14,2) NOT NULL DEFAULT 0,
    conversion_rate     NUMERIC(8,6),
    engagement_rate     NUMERIC(8,6),
    cost                NUMERIC(14,2) NOT NULL DEFAULT 0,
    roas                NUMERIC(10,4),
    notes               TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_content_performance_content_id ON content_performance(generated_content_id);
CREATE INDEX idx_content_performance_channel    ON content_performance(channel);
CREATE INDEX idx_content_performance_published  ON content_performance(published_at);

CREATE TABLE content_insights (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    generated_content_id UUID         REFERENCES generated_content(id) ON DELETE SET NULL,
    campaign_id          UUID         REFERENCES campaigns(id) ON DELETE SET NULL,
    insight_type         VARCHAR(50)  NOT NULL,
    insight_text         TEXT         NOT NULL,
    confidence_score     NUMERIC(5,4) NOT NULL DEFAULT 0,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_insight_type CHECK (
        insight_type IN (
            'WINNING_HOOK','WINNING_ANGLE','LOW_PERFORMING_ANGLE',
            'STRONG_CTA','WEAK_CTA','AUDIENCE_SIGNAL','CHANNEL_SIGNAL'
        )
    ),
    CONSTRAINT chk_confidence_score CHECK (confidence_score >= 0 AND confidence_score <= 1)
);

CREATE INDEX idx_content_insights_type       ON content_insights(insight_type);
CREATE INDEX idx_content_insights_content_id ON content_insights(generated_content_id);
CREATE INDEX idx_content_insights_campaign   ON content_insights(campaign_id);

CREATE TABLE performance_summaries (
    id                  UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    period_start        DATE      NOT NULL,
    period_end          DATE      NOT NULL,
    channel             VARCHAR(50),
    summary_text        TEXT      NOT NULL,
    recommended_angles  TEXT,
    recommended_hooks   TEXT,
    recommended_ctas    TEXT,
    avoid_patterns      TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_period CHECK (period_end >= period_start)
);

CREATE INDEX idx_performance_summaries_period  ON performance_summaries(period_start, period_end);
CREATE INDEX idx_performance_summaries_channel ON performance_summaries(channel);
