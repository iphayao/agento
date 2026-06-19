# Prompt 02 — LangGraph Agentic Workflow

```text
You are an expert AI engineer and full-stack system architect.

Read CLAUDE.md first.
Review the existing Phase 1 implementation before editing.

Implement only Phase 2 of Agento: LangGraph Agentic Workflow.

Goal:
Add a Python FastAPI + LangGraph agento-worker service to handle multi-step content generation workflows.

Architecture rule:
- Spring Boot API remains the system of record.
- Python agento-worker handles AI workflow execution only.

Agent workflow input:
- campaignId
- brand profile
- product facts
- campaign objective
- target audience
- content channel
- content angle

Workflow steps:
1. Brand Strategist Agent
2. Customer Insight Agent
3. Content Writer Agent
4. Claim & Compliance Agent
5. Editor Agent
6. Final Output Formatter

Claim & Compliance Agent must check risky claims such as:
- "ไร้ฝุ่น"
- "ปลอดภัย"
- "สะอาดที่สุด"
- medical or health claims

Human-in-the-loop:
- Workflow must not auto-publish.
- Final content status must be DRAFT.
- Founder must approve manually.

Backend requirements:
Add AgentWorkflow:
- id
- campaignId
- status: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
- currentStep
- inputPayload
- outputPayload
- errorMessage
- createdAt
- updatedAt

Add AgentStepResult:
- id
- workflowId
- stepName
- status
- inputPayload
- outputPayload
- errorMessage
- startedAt
- completedAt

Add APIs:
- POST /api/campaigns/{id}/agent-workflows
- GET /api/agent-workflows/{id}
- GET /api/agent-workflows/{id}/steps
- POST /api/agent-workflows/{id}/retry
- POST /api/agent-workflows/{id}/cancel

Agento-worker requirements:
1. Create FastAPI service.
2. Implement LangGraph workflow.
3. Use typed state object.
4. Use Pydantic models.
5. Use structured JSON output.
6. Add retry handling for failed LLM calls.
7. Add endpoint: POST /workflows/content-generation
8. Return workflow status, steps, finalContent, and complianceWarnings.

Frontend requirements:
1. Add Run Agent Workflow button on campaign detail page.
2. Show workflow status.
3. Show step results.
4. Show compliance warnings.
5. Show final generated content.
6. Allow approve/reject after completion.

Acceptance criteria:
1. User can run agent workflow from campaign detail page.
2. LangGraph executes workflow steps.
3. Step results are visible.
4. Compliance warnings are visible.
5. Final content is saved as DRAFT.
6. Failed workflow can be retried.

Do not implement:
- RAG
- Performance learning
- Calendar
- Export

Before editing:
1. Explain task goal.
2. List files to create or modify.
3. Confirm architecture rules.
4. Produce implementation plan.

After editing:
1. Summarize changes.
2. List tests added.
3. Explain how to run agento-worker locally.
4. Recommend running the review prompt next.
```
