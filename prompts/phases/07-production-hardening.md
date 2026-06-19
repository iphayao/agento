# Prompt 07 — Production Hardening

```text
You are an expert production software engineer.

Read CLAUDE.md first.
Review the existing implementation before editing.

Implement only Phase 7 of Agento: Production Hardening.

Goal:
Make Agento reliable, secure, observable, and production-ready.

Security requirements:
1. Add authentication and authorization.
2. Use role-based access: ADMIN, EDITOR, VIEWER
3. Protect backend APIs.
4. Store API keys securely.
5. Never expose LLM keys to frontend.
6. Add request validation.
7. Add rate limiting for AI generation APIs.
8. Add audit logs for content generation, approval, rejection, export, and knowledge changes.

Observability requirements:
1. Add structured logging.
2. Add correlation ID across frontend, backend, and agento-worker.
3. Add OpenTelemetry tracing.
4. Add metrics for AI call count, AI latency, failed generations, workflow success rate, token usage, and cost estimate.

Reliability requirements:
1. Add retry with backoff for LLM calls.
2. Add timeout handling.
3. Add idempotency key for generation requests.
4. Prevent duplicate generation when user clicks multiple times.
5. Add background job recovery.
6. Add database indexes.
7. Add health checks for frontend, backend, agento-worker, database, and redis.

Testing requirements:
1. Backend unit tests.
2. Backend integration tests using Testcontainers.
3. Agento-worker unit tests.
4. Contract tests between Spring Boot and agento-worker.
5. Frontend component tests.
6. E2E test for create campaign, run agent workflow, approve content, export content.

Deployment requirements:
1. Improve Dockerfiles.
2. Add production Docker Compose.
3. Add Kubernetes manifests or Helm chart skeleton.
4. Add GitHub Actions CI.
5. Add environment-specific configuration for local, dev, and prod.

Acceptance criteria:
1. APIs require authentication.
2. Roles work correctly.
3. AI generation is rate limited.
4. Workflow has retry and timeout handling.
5. Logs contain correlation IDs.
6. Tests cover core user flows.
7. Docker images build successfully.
8. System has health checks.
9. Production deployment documentation exists.

Before editing:
1. Explain task goal.
2. List files to create or modify.
3. Confirm architecture rules.
4. Produce implementation plan.

After editing:
1. Summarize changes.
2. List tests added.
3. Explain security model.
4. Explain production deployment notes.
```
