# Agento — Architecture

## Overview

Agento is a three-tier monorepo with a clear separation of concerns:

| App | Role | Tech |
|---|---|---|
| `agento-web` | Internal operator dashboard | Next.js 14 + TypeScript |
| `agento-api` | System of record, REST API | Spring Boot 3 + Java 21 |
| `agento-worker` | AI agent runtime | Python + FastAPI + LangGraph |

Infrastructure services run locally via Docker Compose and in production via Kubernetes.

---

## Component Diagram

```
Browser (agento-web)
       │
       │ HTTPS / REST
       ▼
agento-api  ◄────────────────────► PostgreSQL
  (Spring Boot 3)                  Redis
       │
       │ HTTP (internal)
       ▼
agento-worker
  (FastAPI + LangGraph)
       │
       │ HTTPS
       ▼
LLM Provider (Anthropic / OpenAI — via abstraction)
       │
       ▼
MinIO (generated content artifacts)
```

---

## Key Design Decisions

### 1. Spring Boot as System of Record
All persistent state lives in PostgreSQL and is owned by `agento-api`. The worker is stateless from a persistence perspective — it receives a job, runs the AI workflow, and posts results back via REST callback.

### 2. Provider Abstraction for AI
The worker wraps LLM calls behind a thin provider interface. Swapping from Anthropic to OpenAI or another provider requires only a config change, not a code change in the workflow logic.

### 3. LLM Keys Never Reach the Frontend
`agento-web` talks only to `agento-api`. The API delegates to the worker. API keys stay server-side.

### 4. Content Always Starts as DRAFT
Generated content is stored with status `DRAFT`. A human operator must approve before it is exported or published.

### 5. Claim Safety at the Worker Layer
Before the worker finalises any content output it runs a claim-check step that flags or blocks phrases on the banned-claims list (see `packages/brand-kit`).

---

## Data Flow — Content Generation

```
1. Operator creates ContentBrief in agento-web
2. agento-api stores brief, dispatches AgentTask to agento-worker
3. agento-worker runs LangGraph workflow:
     a. Load brand context (brand-kit + prompt-library)
     b. Generate draft content (LLM call)
     c. Claim-safety check
     d. Structure output as JSON (content-schemas)
     e. POST result to agento-api callback
4. agento-api stores result with status DRAFT
5. Operator reviews and approves in agento-web
6. Approved content is exported for manual publishing
```

---

## Database

- **PostgreSQL** — all relational data (brands, products, briefs, campaigns, content, agent runs)
- **pgvector** — added when RAG / brand-memory phase begins (TODO: Phase 3)
- **Flyway** — manages all schema migrations in `agento-api`

## Packages

| Package | Purpose |
|---|---|
| `brand-kit` | SoClean brand voice, positioning, claim rules, tone guide |
| `prompt-library` | Versioned LLM prompt templates for each content type |
| `content-schemas` | Shared JSON schemas / TypeScript types / Pydantic models |

---

## TODO — Future Architecture

- [ ] pgvector for semantic brand memory (Phase 3)
- [ ] Redis pub/sub for async agent task queue (Phase 2+)
- [ ] MinIO for storing generated image assets
- [ ] Kubernetes deployment with Helm charts
- [ ] Observability: structured logging, traces, LLM cost tracking
