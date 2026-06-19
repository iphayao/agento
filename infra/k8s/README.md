# infra/k8s

Kubernetes manifests for Agento production deployment.

## Status

TODO — Planned for Phase 7 (Production Hardening).

## Planned Structure

```
k8s/
  base/
    namespace.yaml
    postgres/
    redis/
    minio/
    agento-api/
    agento-web/
    agento-worker/
  overlays/
    dev/
    prod/
```

Kustomize will be used to manage environment overlays.
