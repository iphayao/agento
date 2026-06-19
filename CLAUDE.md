# CLAUDE.md

You are working on **Agento**.

Agento is an **Agentic AI Content System** for BN Paper, focused first on the **SoClean** facial tissue brand.

## Business Context

Company:
- BN Paper

Primary brand:
- SoClean

Product:
- Facial tissue
- 2-ply
- 180 sheets
- Soft
- Clean
- Low-dust / dust-free positioning
- Pack of 5
- Carton of 50 packs

Thai positioning:
- "สะอาด เนียนนุ่ม ไร้ฝุ่น"

Main channels:
- TikTok Shop
- Shopee
- Lazada
- Facebook
- Reseller stores

Target customers:
- Women Gen Y with purchasing power
- Households
- Office buyers
- Shops and resellers buying by carton

Customer review themes:
- Soft
- Thick enough
- Fast delivery
- Good value
- Good for resale

## System Goal

Build Agento as an internal AI-powered marketing content system that can:

1. Manage brand profile
2. Manage product facts
3. Create campaigns
4. Generate marketing content
5. Run multi-step agentic content workflows
6. Check claim and compliance risks
7. Store brand memory
8. Learn from content performance
9. Plan content calendars
10. Export approved content for manual publishing

## Preferred Architecture

```text
agento/
  apps/
    agento-web/        # Next.js + TypeScript
    agento-api/        # Spring Boot 3
    agento-worker/     # Python + FastAPI + LangGraph
  packages/
    brand-kit/
    prompt-library/
    content-schemas/
  infra/
    docker-compose/
    k8s/
    helm/
  docs/
```

## Technical Preferences

Frontend:
- Next.js
- TypeScript
- React Hook Form
- Zod validation
- shadcn/ui or MUI

Backend:
- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL
- Redis
- DTOs, services, repositories, validators

Agent Worker:
- Python
- FastAPI
- LangGraph
- Pydantic
- Typed workflow state
- Structured JSON outputs

AI:
- Use provider abstraction.
- Do not hard-code one LLM provider into business logic.
- Never expose LLM API keys to frontend.
- Prefer structured JSON output.
- Validate all AI responses before saving.

Database:
- PostgreSQL first.
- Use pgvector when the RAG phase starts.
- Use Flyway migrations for schema changes.

## Coding Rules

1. Stay inside the current phase scope.
2. Do not implement future phases early.
3. Do not create unnecessary microservices.
4. Keep Spring Boot API as the system of record.
5. Keep Python agento-worker focused on AI workflow execution.
6. Keep generated content as DRAFT until human approval.
7. Use claim-safe marketing language.
8. Avoid unsupported absolute claims.
9. Add migrations for database changes.
10. Add or update tests when behavior changes.
11. Update documentation when setup or behavior changes.
12. Do not over-engineer for enterprise scale too early.
13. Prioritize practical implementation for a solo founder.

## Claim Safety Rules

Avoid unproven absolute claims:
- 100% dust-free
- medically safe
- antibacterial
- hypoallergenic
- safest
- cleanest
- สะอาดที่สุด
- ปลอดภัยที่สุด

Prefer safer wording:
- ฝุ่นน้อย
- ให้สัมผัสสะอาด
- เนียนนุ่ม
- เหมาะกับการใช้งานทุกวัน
- คุ้มค่าสำหรับบ้าน ร้านค้า และออฟฟิศ

## Expected Claude Code Behavior

Before editing:
1. Explain the task goal.
2. List files you plan to create or modify.
3. Confirm architecture rules.
4. Produce a short implementation plan.

During editing:
1. Implement only the requested phase.
2. Keep code clean and testable.
3. Add meaningful tests.
4. Avoid implementing future phases.
5. Update docs when needed.

After editing:
1. Summarize what changed.
2. List files changed.
3. List tests added or updated.
4. Explain how to run locally.
5. Mention remaining known issues.
6. Suggest the next prompt to run.
