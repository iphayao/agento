CREATE TABLE export_jobs (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    export_type    VARCHAR(50)  NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    calendar_id    UUID,
    campaign_id    UUID,
    channel        VARCHAR(100),
    start_date     DATE,
    end_date       DATE,
    include_statuses TEXT,
    file_url       TEXT,
    file_name      VARCHAR(255),
    row_count      INTEGER,
    error_message  TEXT,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_export_jobs_created_at ON export_jobs (created_at DESC);
CREATE INDEX idx_export_jobs_status     ON export_jobs (status);
