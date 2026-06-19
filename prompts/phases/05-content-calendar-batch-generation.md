# Prompt 05 — Content Calendar and Batch Generation

```text
You are an expert product engineer and AI workflow engineer.

Read CLAUDE.md first.
Review the existing implementation before editing.

Implement only Phase 5 of Agento: Content Calendar and Batch Generation.

Goal:
Add a content calendar and batch generation so the founder can plan and generate content for a full week or month.

New entities:
1. ContentCalendar
   - id
   - name
   - periodStart
   - periodEnd
   - objective
   - status: DRAFT, GENERATING, READY_FOR_REVIEW, APPROVED
   - createdAt
   - updatedAt

2. CalendarItem
   - id
   - calendarId
   - plannedDate
   - channel
   - contentType
   - contentAngle
   - targetAudience
   - status
   - generatedContentId
   - createdAt
   - updatedAt

3. BatchGenerationJob
   - id
   - calendarId
   - status: PENDING, RUNNING, COMPLETED, FAILED
   - totalItems
   - completedItems
   - failedItems
   - errorMessage
   - createdAt
   - updatedAt

Backend requirements:
1. Add CRUD APIs for content calendar.
2. Add APIs for calendar items.
3. Add batch generation API: POST /api/content-calendars/{id}/generate
4. Use async processing.
5. Use Redis queue or backend job table.
6. Each calendar item generates content through existing agent workflow.
7. Save each generated result as GeneratedContent.
8. Track batch job progress.

Agento-worker requirements:
Add Calendar Planner Agent that suggests date, channel, content type, content angle, hook direction, and CTA direction.

Frontend requirements:
1. Calendar list
2. Create calendar
3. Calendar detail
4. Calendar view by week/month
5. Generate calendar plan
6. Generate content for all items
7. Show batch generation progress
8. Review generated content per calendar item
9. Approve/reject per item

Acceptance criteria:
1. User can create weekly/monthly calendar.
2. System can suggest calendar items.
3. User can edit calendar items before generation.
4. User can batch-generate content.
5. Progress is visible.
6. Generated content is linked to calendar items.
7. User can approve/reject each item.

Before editing:
1. Explain task goal.
2. List files to create or modify.
3. Confirm architecture rules.
4. Produce implementation plan.

After editing:
1. Summarize changes.
2. List tests added.
3. Explain how batch progress works.
4. Recommend next prompt.
```
