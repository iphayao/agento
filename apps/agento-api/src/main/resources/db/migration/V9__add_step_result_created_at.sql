-- Phase 3 fix: track when each agent step record was created
-- Existing rows receive the current timestamp as a reasonable default.
ALTER TABLE agent_step_results
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();
