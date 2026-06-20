-- Phase 2: Agent workflow tables for LangGraph multi-step content generation

CREATE TABLE agent_workflows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    current_step VARCHAR(100),
    input_payload TEXT,
    output_payload TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_agent_workflow_status
        CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_agent_workflows_campaign_id ON agent_workflows(campaign_id);
CREATE INDEX idx_agent_workflows_status ON agent_workflows(status);

CREATE TABLE agent_step_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES agent_workflows(id) ON DELETE CASCADE,
    step_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    input_payload TEXT,
    output_payload TEXT,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT chk_agent_step_status
        CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'SKIPPED'))
);

CREATE INDEX idx_agent_step_results_workflow_id ON agent_step_results(workflow_id);

-- Link generated content back to the workflow that produced it (nullable — existing rows unaffected)
ALTER TABLE generated_content
    ADD COLUMN workflow_id UUID REFERENCES agent_workflows(id) ON DELETE SET NULL;
