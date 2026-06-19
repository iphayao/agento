# agento-web

Internal operator dashboard for Agento.

**Tech:** Next.js 14 + TypeScript + shadcn/ui + React Hook Form + Zod

## Status

TODO — App scaffold created in Phase 1.

## Planned Features

- Brand and product management
- Campaign and content brief management
- AI agent run monitoring
- Content draft review and approval
- Content export for publishing

## Development

```bash
pnpm install
cp .env.example .env.local
pnpm dev          # http://localhost:3000
pnpm build
pnpm test
```

## Structure (planned)

```
agento-web/
  src/
    app/           # Next.js App Router pages
    components/    # Shared UI components
    lib/           # API client, utilities
    hooks/         # Custom React hooks
  public/
  .env.example
```
