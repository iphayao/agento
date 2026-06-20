-- FK: generated_content.campaign_id -> campaigns(id)
-- ON DELETE CASCADE removes content when a campaign is deleted
ALTER TABLE generated_content
    ADD CONSTRAINT fk_generated_content_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns (id) ON DELETE CASCADE;

-- Constrain campaign status to the four supported values
ALTER TABLE campaigns
    ADD CONSTRAINT chk_campaign_status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'ARCHIVED'));

-- Constrain generated_content status to the three supported enum values
ALTER TABLE generated_content
    ADD CONSTRAINT chk_generated_content_status
        CHECK (status IN ('DRAFT', 'APPROVED', 'REJECTED'));
