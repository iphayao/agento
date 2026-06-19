# infra/docker-compose

Local development infrastructure for Agento.

## Services

| Service | Image | Host Port | Notes |
|---|---|---|---|
| PostgreSQL | postgres:16-alpine | 54320 | Avoid conflict with local Postgres on 5432 |
| Redis | redis:7-alpine | 6379 | |
| MinIO | minio/minio:latest | 9000 (API), 9001 (console) | S3-compatible storage |

## Usage

```bash
cp .env.example .env
docker compose up -d
docker compose ps       # check status
docker compose logs -f  # stream logs
docker compose down     # stop
docker compose down -v  # stop and delete all data volumes
```

## MinIO Console

http://localhost:9001

Default credentials: `agento` / `agento-secret`

## TODO

- [ ] Add init container to create default MinIO buckets on first run
- [ ] Add pgvector extension init SQL for Phase 3 (RAG)
