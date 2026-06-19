# Prompt 01 — MVP Content Generator

```text
You are an expert full-stack engineer.

Read CLAUDE.md first.
Read packages/brand-kit/ and packages/prompt-library/ before implementation.

Implement only Phase 1 of Agento: MVP Content Generator.

Goal:
Build a basic MVP where the founder can:
1. Manage brand profile
2. Manage product facts
3. Create marketing campaigns
4. Generate TikTok captions/scripts using an LLM
5. Save generated content
6. Review and approve/reject content manually

Technology:
- Frontend: Next.js + TypeScript
- Backend: Spring Boot 3 + Java 21
- Database: PostgreSQL
- ORM: Spring Data JPA
- Migration: Flyway
- AI integration: provider abstraction first, OpenAI-compatible implementation second
- Local development: Docker Compose

Backend package modules:
- brand
- product
- campaign
- content
- ai
- common

Entities:
1. BrandProfile: id, brandName, slogan, toneOfVoice, targetAudience, keyMessages, prohibitedClaims, createdAt, updatedAt
2. ProductFact: id, productName, sku, sheetCount, ply, packSize, cartonSize, keyBenefits, proofPoints, createdAt, updatedAt
3. Campaign: id, name, objective, channel, targetAudience, contentAngle, status, createdAt, updatedAt
4. GeneratedContent: id, campaignId, contentType, channel, title, body, hook, callToAction, hashtags, status, aiModel, promptVersion, complianceNotes, createdAt, updatedAt

GeneratedContent status:
- DRAFT
- APPROVED
- REJECTED

REST APIs:
- CRUD brand profile
- CRUD product facts
- CRUD campaigns
- Generate content for a campaign
- List generated content
- Approve content
- Reject content

AI requirements:
- Create AiProvider interface.
- Create OpenAiCompatibleProvider implementation.
- Do not expose API key to frontend.
- Prompt must use brand profile, product facts, campaign objective, channel, and content angle.
- Return structured JSON.
- Validate JSON response before saving.
- If AI response is invalid, return clear error and do not save invalid content.
- Generated content must be DRAFT by default.

Frontend screens:
1. Dashboard
2. Brand Profile
3. Product Facts
4. Campaigns
5. Campaign Detail
6. Generated Content Review

Acceptance criteria:
1. User can create brand profile.
2. User can create product facts.
3. User can create campaign.
4. User can click Generate Content.
5. Backend calls LLM provider.
6. Generated content is saved in PostgreSQL.
7. Generated content defaults to DRAFT.
8. User can approve/reject generated content.
9. App runs locally with Docker Compose.
10. Backend has basic unit tests for important service logic.

Do not implement:
- LangGraph
- RAG
- Performance learning
- Content calendar
- Export

Before editing:
1. Explain the task goal.
2. List files you plan to create or modify.
3. Confirm architecture rules.
4. Produce a short implementation plan.

After editing:
1. Summarize what changed.
2. List files changed.
3. Explain how to run locally.
4. List tests added.
5. Recommend running the review prompt next.
```
