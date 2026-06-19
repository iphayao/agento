# Prompt 00 — Create Agento Repository Structure

```text
You are an expert full-stack software architect and senior engineer.

Read CLAUDE.md first.

Implement only the initial repository structure for Agento.

Business context:
Agento is an Agentic AI Content System for BN Paper, focused first on SoClean facial tissue marketing content generation.

Recommended architecture:
- Frontend: Next.js + TypeScript
- Backend API: Spring Boot 3 + Java 21
- Agent worker: Python + FastAPI + LangGraph
- Database: PostgreSQL
- Vector search later: pgvector
- Cache/queue: Redis
- Storage: MinIO/S3
- Local development: Docker Compose
- Future deployment: Kubernetes/Helm

Create this monorepo structure:

agento/
  apps/
    agento-web/
    agento-api/
    agento-worker/
  packages/
    brand-kit/
    prompt-library/
    content-schemas/
  infra/
    docker-compose/
    k8s/
    helm/
  docs/

Requirements:
1. Create README.md explaining Agento.
2. Create docs/architecture.md.
3. Create docs/local-dev.md.
4. Create docs/implementation-roadmap.md.
5. Create Docker Compose for PostgreSQL, Redis, and MinIO.
6. Add .env.example files for agento-web, agento-api, and agento-worker.
7. Add placeholder README.md files in important folders.
8. Add .gitignore.
9. Do not implement business logic yet.
10. Do not create full app scaffolds unless needed for structure.
11. Add clear TODO markers for future phases.

Before editing:
1. Explain the task goal.
2. List files you plan to create.
3. Confirm architecture rules.
4. Produce a short implementation plan.

After editing:
1. Summarize what was created.
2. Explain how to run local dependencies.
3. Recommend the next prompt to run.
```
