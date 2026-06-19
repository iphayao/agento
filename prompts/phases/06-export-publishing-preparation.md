# Prompt 06 — Export and Publishing Preparation

```text
You are an expert full-stack engineer.

Read CLAUDE.md first.
Review the existing implementation before editing.

Implement only Phase 6 of Agento: Export and Publishing Preparation.

Goal:
Add export and publishing preparation features.

Important:
Do not auto-publish to TikTok, Shopee, Lazada, or Facebook yet.
The system should prepare approved content for manual publishing.

New entity:
ExportJob
- id
- exportType: CONTENT_CSV, CALENDAR_CSV, VIDEO_SCRIPT_PACK, RESELLER_CAPTION_PACK, MARKETPLACE_COPY_PACK
- status: PENDING, RUNNING, COMPLETED, FAILED
- fileUrl
- errorMessage
- createdAt
- updatedAt

Backend requirements:
1. Add export APIs:
   - POST /api/exports/content
   - POST /api/exports/calendar/{calendarId}
   - GET /api/exports/{id}
2. Generate CSV files.
3. Store files in MinIO/S3.
4. Return downloadable URL.
5. Only export APPROVED content by default.
6. Allow filters by date range, channel, campaign, and calendar.

Frontend requirements:
1. Export approved content
2. Export by calendar
3. Export by campaign
4. Download CSV
5. Download video script pack
6. Download reseller caption pack

CSV columns:
- Date
- Channel
- Campaign
- Content Type
- Hook
- Body
- CTA
- Hashtags
- Compliance Notes
- Status

Acceptance criteria:
1. User can export approved content.
2. User can export content calendar.
3. User can download CSV.
4. Export file is stored in MinIO/S3.
5. Export history is visible.
6. Draft/rejected content is excluded unless explicitly selected.

Before editing:
1. Explain task goal.
2. List files to create or modify.
3. Confirm architecture rules.
4. Produce implementation plan.

After editing:
1. Summarize changes.
2. List tests added.
3. Explain export file storage.
4. Recommend next prompt.
```
