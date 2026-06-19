# infra/helm

Helm charts for Agento production deployment.

## Status

TODO — Planned for Phase 7 (Production Hardening).

## Planned Charts

```
helm/
  agento/           # umbrella chart
    Chart.yaml
    values.yaml
    values.prod.yaml
    charts/
      agento-api/
      agento-web/
      agento-worker/
```

Third-party dependencies (PostgreSQL, Redis, MinIO) will be pulled via Bitnami Helm charts.
