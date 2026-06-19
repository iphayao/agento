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

## Documentation

- [Architecture](docs/architecture.md)
- [Local Development](docs/local-dev.md)
- [Implementation Roadmap](docs/implementation-roadmap.md)

## Brand

**SoClean** facial tissue — *สะอาด เนียนนุ่ม ฝุ่นน้อย*

Target: Women Gen Y, households, office buyers, resellers.
Channels: TikTok Shop, Shopee, Lazada, Facebook, reseller stores.
