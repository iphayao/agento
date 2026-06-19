# agento-api

System-of-record REST API for Agento.

**Tech:** Spring Boot 3 + Java 21 + PostgreSQL + Redis + Flyway

## Status

TODO — App scaffold created in Phase 1.

## Responsibilities

- Persist all domain data (brands, products, campaigns, briefs, content, agent runs)
- Expose REST API consumed by agento-web
- Dispatch AI tasks to agento-worker
- Receive AI results via callback endpoint
- Enforce claim-safety rules before persisting generated content

## Development

```bash
cp .env.example .env
./mvnw spring-boot:run    # or: ./gradlew bootRun
# API: http://localhost:8080
# Health: http://localhost:8080/actuator/health
```

## Structure (planned)

```
agento-api/
  src/main/java/com/bnpaper/agento/
    brand/          # Brand + ProductLine + Product
    campaign/       # Campaign + Channel
    brief/          # ContentBrief
    content/        # GeneratedContent + review workflow
    agent/          # AgentTask + AgentRun + dispatch
    common/         # DTOs, validators, exceptions
  src/main/resources/
    db/migration/   # Flyway SQL migrations
    application.yml
```
