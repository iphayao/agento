# Agento Claude Code Usage Order

## Recommended Order

```text
1. prompts/foundation/00-create-repository-structure.md
2. prompts/foundation/09-create-soclean-brand-kit.md
3. prompts/foundation/08-create-prompt-library.md
4. prompts/phases/01-mvp-content-generator.md
5. prompts/review/10-system-design-review.md
6. prompts/fix/fix-review-issues.md
7. prompts/review/10-system-design-review.md
8. prompts/phases/02-langgraph-agentic-workflow.md
9. prompts/review/10-system-design-review.md
10. prompts/fix/fix-review-issues.md
11. prompts/phases/03-brand-memory-rag.md
12. prompts/review/10-system-design-review.md
13. prompts/fix/fix-review-issues.md
14. prompts/phases/04-performance-learning.md
15. prompts/phases/05-content-calendar-batch-generation.md
16. prompts/phases/06-export-publishing-preparation.md
17. prompts/phases/07-production-hardening.md
```

## Best First MVP Sequence

```text
00-create-repository-structure
09-create-soclean-brand-kit
08-create-prompt-library
01-mvp-content-generator
10-system-design-review
fix-review-issues
10-system-design-review
```

## Commit Strategy

Commit each phase separately.

```bash
git add .
git commit -m "chore: initialize agento repository structure"

git add .
git commit -m "docs: add soclean brand kit"

git add .
git commit -m "docs: add agento prompt library"

git add .
git commit -m "feat: add mvp content generator"

git add .
git commit -m "fix: address mvp review issues"
```
