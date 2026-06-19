# packages/content-schemas

Shared content data contracts used across agento-api, agento-web, and agento-worker.

Defines the shape of generated content objects so all three apps agree on structure without duplicating type definitions.

## Planned Structure

```
content-schemas/
  json/
    content-draft.schema.json     # JSON Schema for a generated content draft
    content-brief.schema.json     # JSON Schema for a content brief
    agent-task.schema.json        # JSON Schema for an agent task payload
  typescript/
    content.ts                    # TypeScript types (used in agento-web)
  python/
    content.py                    # Pydantic models (used in agento-worker)
```

The TypeScript and Python models are derived from the canonical JSON Schema.

## Key Types (planned)

- `ContentBrief` — operator input: channel, objective, product, tone hints
- `ContentDraft` — AI output: headline, body, hashtags, claim flags, status
- `AgentTaskPayload` — dispatched to worker: brief + brand context
- `AgentResultPayload` — returned from worker: draft + claim check result

## TODO

- [ ] Define `content-draft.schema.json` (Phase 1)
- [ ] Define `content-brief.schema.json` (Phase 1)
- [ ] Generate TypeScript types from JSON Schema (Phase 1)
- [ ] Generate Pydantic models from JSON Schema (Phase 1)
