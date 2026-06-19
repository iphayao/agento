# Agento — Implementation Roadmap

## Phase 0 — Repository Structure (current)

Goal: Establish the monorepo skeleton, local dev stack, and documentation.

- [x] Directory structure
- [x] Docker Compose (PostgreSQL, Redis, MinIO)
- [x] `.env.example` files
- [x] `.gitignore`
- [x] Core documentation
- [ ] TODO: Initialize agento-api Spring Boot project
- [ ] TODO: Initialize agento-web Next.js project
- [ ] TODO: Initialize agento-worker FastAPI project

---

## Phase 1 — MVP Content Generator

Goal: End-to-end flow from content brief to generated draft.

- [ ] Brand and product CRUD (agento-api)
- [ ] ContentBrief CRUD (agento-api)
- [ ] Basic campaign and channel management
- [ ] Simple LangGraph workflow: brief → LLM → draft content
- [ ] Draft content stored in PostgreSQL
- [ ] Operator dashboard to view drafts (agento-web)
- [ ] Claim-safety check (basic keyword filter)

---

## Phase 2 — LangGraph Agentic Workflow

Goal: Multi-step autonomous content generation with human review.

- [ ] AgentTask + AgentRun tracking (agento-api)
- [ ] Multi-step LangGraph graph: research → draft → review → refine
- [ ] Async dispatch via Redis queue (or HTTP callback)
- [ ] Agent run status visible in agento-web
- [ ] Retry and error handling for failed runs

---

## Phase 3 — Brand Memory / RAG

Goal: AI-generated content that "remembers" brand voice and past decisions.

- [ ] pgvector enabled in PostgreSQL
- [ ] Embedding pipeline for brand-kit content
- [ ] Semantic retrieval at generation time
- [ ] Brand memory management UI

---

## Phase 4 — Performance Learning

Goal: Use past content performance to improve future generation.

- [ ] Content performance data ingestion (manual + CSV import)
- [ ] Performance-weighted prompt selection
- [ ] Reporting dashboard

---

## Phase 5 — Content Calendar & Batch Generation

Goal: Plan and generate content for a full campaign period.

- [ ] Calendar UI in agento-web
- [ ] Batch brief creation by channel and date
- [ ] Parallel agent runs for batch generation
- [ ] Calendar export (Google Sheets / CSV)

---

## Phase 6 — Export & Publishing Preparation

Goal: Package approved content for manual publishing on each channel.

- [ ] Export formats per channel (TikTok, Shopee, Lazada, Facebook)
- [ ] Asset packaging with MinIO
- [ ] Download bundles (text + image brief)

---

## Phase 7 — Production Hardening

Goal: Make the system production-ready for a solo founder operating it.

- [ ] Kubernetes manifests + Helm charts
- [ ] Secrets management (sealed secrets or external)
- [ ] CI/CD pipeline
- [ ] Structured logging + observability
- [ ] LLM cost tracking
- [ ] Backup and restore runbook

---

## Claim Safety (all phases)

At every content-generation phase, the worker must:
1. Never use banned absolute claims (100% dust-free, antibacterial, etc.)
2. Prefer safe Thai wording (ฝุ่นน้อย, เนียนนุ่ม, เหมาะกับการใช้งานทุกวัน)
3. Flag uncertain claims for human review rather than blocking generation

See `packages/brand-kit` for the authoritative claim rules.
