-- Phase 7: database indexes for common query patterns

-- Generated content
CREATE INDEX IF NOT EXISTS idx_generated_content_campaign_id ON generated_content (campaign_id);
CREATE INDEX IF NOT EXISTS idx_generated_content_status      ON generated_content (status);
CREATE INDEX IF NOT EXISTS idx_generated_content_workflow_id ON generated_content (workflow_id);
CREATE INDEX IF NOT EXISTS idx_generated_content_created_at  ON generated_content (created_at DESC);

-- Campaigns
CREATE INDEX IF NOT EXISTS idx_campaigns_status     ON campaigns (status);
CREATE INDEX IF NOT EXISTS idx_campaigns_created_at ON campaigns (created_at DESC);

-- Agent workflows
CREATE INDEX IF NOT EXISTS idx_agent_workflows_campaign_id  ON agent_workflows (campaign_id);
CREATE INDEX IF NOT EXISTS idx_agent_workflows_status       ON agent_workflows (status);
CREATE INDEX IF NOT EXISTS idx_agent_workflows_created_at   ON agent_workflows (created_at DESC);

-- Agent step results
CREATE INDEX IF NOT EXISTS idx_agent_step_results_workflow_id ON agent_step_results (workflow_id);
CREATE INDEX IF NOT EXISTS idx_agent_step_results_status      ON agent_step_results (status);

-- Calendar items
CREATE INDEX IF NOT EXISTS idx_calendar_items_calendar_id    ON calendar_items (calendar_id);
CREATE INDEX IF NOT EXISTS idx_calendar_items_status         ON calendar_items (status);
CREATE INDEX IF NOT EXISTS idx_calendar_items_planned_date   ON calendar_items (planned_date);
CREATE INDEX IF NOT EXISTS idx_calendar_items_workflow_id    ON calendar_items (workflow_id);

-- Knowledge
CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_document_id  ON knowledge_chunks (document_id);

-- Export jobs
CREATE INDEX IF NOT EXISTS idx_export_jobs_status      ON export_jobs (status);
CREATE INDEX IF NOT EXISTS idx_export_jobs_created_at  ON export_jobs (created_at DESC);

-- Performance (columns generated_content_id / published_at; channel already indexed in V10)
CREATE INDEX IF NOT EXISTS idx_content_performance_gen_content_id ON content_performance (generated_content_id);
CREATE INDEX IF NOT EXISTS idx_content_performance_published_at   ON content_performance (published_at DESC);
