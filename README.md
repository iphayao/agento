# Agento

**Agentic AI Content System** for BN Paper — focused first on the **SoClean** facial tissue brand.

Agento is an internal marketing content platform that uses AI agents to generate, review, and manage brand-safe content for Thai e-commerce channels (TikTok Shop, Shopee, Lazada, Facebook).

## System Overview

```
agento/
  apps/
    agento-web/        # Next.js + TypeScript — internal dashboard
    agento-api/        # Spring Boot 3 + Java 21 — system of record
    agento-worker/     # Python + FastAPI + LangGraph — AI agent runtime
  packages/
    brand-kit/         # Brand voice, tone, claim rules for SoClean
    prompt-library/    # Reusable LLM prompt templates
    content-schemas/   # Shared content data contracts (JSON/Zod/Pydantic)
  infra/
    docker-compose/    # Local development stack
    k8s/               # Kubernetes manifests (future)
    helm/              # Helm charts (future)
  docs/
    architecture.md
    local-dev.md
    implementation-roadmap.md
```

## Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 14 + TypeScript + shadcn/ui |
| Backend API | Spring Boot 3 + Java 21 + PostgreSQL |
| Agent Worker | Python + FastAPI + LangGraph |
| Database | PostgreSQL (pgvector later) |
| Cache / Queue | Redis |
| Object Storage | MinIO (S3-compatible) |
| Local Dev | Docker Compose |
| Production | Kubernetes + Helm (planned) |

## Quick Start (Local)

```bash
# Start all infrastructure
cd infra/docker-compose
cp .env.example .env
docker compose up -d

# Then start each app — see docs/local-dev.md
```

## How to Use

### 1. Set Up Brand & Products
- Go to **Brand Profile** — add the SoClean brand info (positioning, tone, target channels)
- Go to **Products** — add your facial tissue product details (pack size, features, pricing)

### 2. Create a Campaign
- Go to **Campaigns** — create a campaign with a goal, target channel, and date range
- A campaign groups all content generated for one marketing push

### 3. Generate Content
- Inside a campaign, trigger the **AI workflow** — runs a 7-step LangGraph agent pipeline:
  `brand_strategist → copywriter → hashtag_expert → claim_checker → formatter`
- All content is saved as **DRAFT** and never published automatically

### 4. Review & Approve Content
- Go to **Content Review** — see all drafts across all campaigns
- Approve or reject each piece before it moves forward

### 5. Plan a Content Calendar
- Go to **Content Calendar** — create a calendar for a time period
- Ask the AI to plan a posting schedule, then batch-generate content for each slot
- Approve or reject individual calendar items

### 6. Learn from Performance
- Go to **Performance** — import CSV data exported from TikTok Shop, Shopee, or Lazada
- The AI generates insights that feed back into the **Knowledge Base** to improve future content

### 7. Export Approved Content
- Go to **Export** — download approved content as CSV for manual publishing
- Supported formats: TikTok, Shopee, Lazada, Facebook, full export

### Knowledge Base
The **Knowledge Base** stores brand memory — product facts, past learnings, and RAG context.
The AI agent reads from it automatically during every content generation run.

---

## Documentation

- [Architecture](docs/architecture.md)
- [Local Development](docs/local-dev.md)
- [Implementation Roadmap](docs/implementation-roadmap.md)

## Brand

**SoClean** facial tissue — *สะอาด เนียนนุ่ม ฝุ่นน้อย*

Target: Women Gen Y, households, office buyers, resellers.
Channels: TikTok Shop, Shopee, Lazada, Facebook, reseller stores.
