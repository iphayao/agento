-- Phase 5: Content Calendar and Batch Generation

CREATE TABLE content_calendars (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    period_start    DATE         NOT NULL,
    period_end      DATE         NOT NULL,
    objective       TEXT,
    status          VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_calendar_status CHECK (
        status IN ('DRAFT', 'GENERATING', 'READY_FOR_REVIEW', 'APPROVED')
    ),
    CONSTRAINT chk_calendar_period CHECK (period_end >= period_start)
);

CREATE INDEX idx_content_calendars_status ON content_calendars(status);
CREATE INDEX idx_content_calendars_period ON content_calendars(period_start, period_end);

CREATE TABLE calendar_items (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    calendar_id           UUID         NOT NULL REFERENCES content_calendars(id) ON DELETE CASCADE,
    planned_date          DATE         NOT NULL,
    channel               VARCHAR(50)  NOT NULL,
    content_type          VARCHAR(100),
    content_angle         TEXT,
    target_audience       TEXT,
    hook_direction        TEXT,
    cta_direction         TEXT,
    status                VARCHAR(30)  NOT NULL DEFAULT 'PLANNED',
    generated_content_id  UUID,
    workflow_id           UUID,
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_item_status CHECK (
        status IN ('PLANNED', 'GENERATING', 'COMPLETED', 'FAILED')
    )
);

CREATE INDEX idx_calendar_items_calendar_id  ON calendar_items(calendar_id);
CREATE INDEX idx_calendar_items_planned_date ON calendar_items(planned_date);
CREATE INDEX idx_calendar_items_status       ON calendar_items(status);

CREATE TABLE batch_generation_jobs (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    calendar_id      UUID         NOT NULL REFERENCES content_calendars(id) ON DELETE CASCADE,
    status           VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    total_items      INT          NOT NULL DEFAULT 0,
    completed_items  INT          NOT NULL DEFAULT 0,
    failed_items     INT          NOT NULL DEFAULT 0,
    error_message    TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_batch_status CHECK (
        status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')
    )
);

CREATE INDEX idx_batch_jobs_calendar_id ON batch_generation_jobs(calendar_id);
CREATE INDEX idx_batch_jobs_status      ON batch_generation_jobs(status);
