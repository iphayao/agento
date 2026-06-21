-- Phase 5: Link workflows and content to calendar items; relax NOT NULL for calendar-only workflows

-- calendar_items now safe to add FK references
ALTER TABLE calendar_items
    ADD CONSTRAINT fk_calendar_items_content
        FOREIGN KEY (generated_content_id) REFERENCES generated_content(id) ON DELETE SET NULL;

ALTER TABLE calendar_items
    ADD CONSTRAINT fk_calendar_items_workflow
        FOREIGN KEY (workflow_id) REFERENCES agent_workflows(id) ON DELETE SET NULL;

-- Allow agent_workflows without a campaign (calendar item workflows have no campaign)
ALTER TABLE agent_workflows
    ALTER COLUMN campaign_id DROP NOT NULL;

ALTER TABLE agent_workflows
    ADD COLUMN calendar_item_id UUID REFERENCES calendar_items(id) ON DELETE SET NULL;

CREATE INDEX idx_agent_workflows_calendar_item ON agent_workflows(calendar_item_id);

-- Allow generated_content without a campaign (calendar item content)
ALTER TABLE generated_content
    ALTER COLUMN campaign_id DROP NOT NULL;

ALTER TABLE generated_content
    ADD COLUMN calendar_item_id UUID REFERENCES calendar_items(id) ON DELETE SET NULL;

CREATE INDEX idx_generated_content_calendar_item ON generated_content(calendar_item_id);
