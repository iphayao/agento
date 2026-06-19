# Agento — Local Development

## Prerequisites

| Tool | Version |
|---|---|
| Docker Desktop | latest |
| Java (JDK) | 21 (set `JAVA_HOME`) |
| Node.js | 20 LTS |
| Python | 3.11+ |
| pnpm | 8+ |

---

## 1. Start Infrastructure

```bash
cd infra/docker-compose
cp .env.example .env          # review and adjust if needed
docker compose up -d
```

Services started:

| Service | Port | Notes |
|---|---|---|
| PostgreSQL | 54320 | host port to avoid conflict with local Postgres |
| Redis | 6379 | |
| MinIO | 9000 / 9001 | API / Console |

Verify:
```bash
docker compose ps
```

MinIO console: http://localhost:9001 (login: `agento` / `agento-secret`)

---

## 2. Start agento-api (Spring Boot)

```bash
cd apps/agento-api
cp .env.example .env           # fill in values
./mvnw spring-boot:run
# or: ./gradlew bootRun
```

API available at: http://localhost:8080

Health check: http://localhost:8080/actuator/health

> Flyway migrations run automatically on startup.

---

## 3. Start agento-worker (Python / FastAPI)

```bash
cd apps/agento-worker
python -m venv .venv
source .venv/bin/activate      # Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env           # add your LLM API key here
uvicorn main:app --reload --port 8001
```

Worker available at: http://localhost:8001

Docs: http://localhost:8001/docs

---

## 4. Start agento-web (Next.js)

```bash
cd apps/agento-web
pnpm install
cp .env.example .env.local
pnpm dev
```

Dashboard available at: http://localhost:3000

---

## Typical Development Workflow

1. `docker compose up -d` — start infra
2. Start `agento-api` (Spring Boot)
3. Start `agento-worker` (FastAPI)
4. Start `agento-web` (Next.js)
5. Open http://localhost:3000

---

## Database

Connect to PostgreSQL:
```bash
psql -h localhost -p 54320 -U agento -d agento
# password: agento
```

Reset database (drops all tables, re-runs Flyway):
```bash
# agento-api handles this via Flyway on startup with clean=true (dev only)
```

---

## Environment Variables Summary

| App | File | Key variables |
|---|---|---|
| agento-web | `.env.local` | `NEXT_PUBLIC_API_URL` |
| agento-api | `.env` | `DATASOURCE_URL`, `REDIS_HOST`, `AGENT_WORKER_URL` |
| agento-worker | `.env` | `AI_PROVIDER`, `ANTHROPIC_API_KEY`, `AGENTO_API_URL` |

---

## Stopping Everything

```bash
# Stop infrastructure
cd infra/docker-compose
docker compose down

# Or remove volumes too (resets database)
docker compose down -v
```
