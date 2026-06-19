# packages/prompt-library

Versioned LLM prompt templates for Agento content workflows.

Prompts are stored as structured templates with variable slots, enabling reuse across channels and content types without duplicating prompt logic in the worker code.

## Planned Structure

```
prompt-library/
  templates/
    product-description/
      shopee-v1.md
      lazada-v1.md
      tiktok-v1.md
    caption/
      facebook-v1.md
      tiktok-v1.md
    claim-check/
      safety-review-v1.md
    campaign-brief/
      brief-to-angles-v1.md
  index.json       # registry of all templates with metadata
```

## Template Format (planned)

Each template file contains:
1. Frontmatter: name, version, channel, content_type, required_variables
2. System prompt section
3. User prompt section with `{{variable}}` slots

## TODO

- [ ] Define template schema (Phase 1)
- [ ] Create first product-description templates (Phase 1)
- [ ] Create claim-check prompt (Phase 1)
- [ ] Version and changelog strategy for prompt updates
