# Agento Claude Code Prompt Kit

This kit contains optimized prompts for implementing **Agento** with Claude Code.

Agento is an **Agentic AI Content System** for BN Paper, focused first on the **SoClean** facial tissue brand.

## Stack

```text
Frontend:      Next.js + TypeScript
Backend API:   Spring Boot 3 + Java 21
Agent Worker:  Python + FastAPI + LangGraph
Database:      PostgreSQL + pgvector
Cache/Queue:   Redis
Storage:       MinIO / S3
Dev:           Docker Compose
```

## How to Use

1. Copy `CLAUDE.md` into your repository root.
2. Open your repository in Claude Code.
3. Use the prompts in `docs/usage-order.md`.
4. Run one prompt at a time.
5. After each implementation phase, run the review prompt.
6. Fix high-priority issues before moving to the next phase.

Do not implement all phases in one Claude Code session.
