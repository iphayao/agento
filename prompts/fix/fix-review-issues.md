# Fix Prompt — Fix Issues From Review

```text
You are a senior full-stack engineer.

Read CLAUDE.md first.
Read the previous architecture/code review result.

Based on the review, fix issues in this priority order:

1. High-priority issues
2. Security concerns
3. Missing critical tests
4. Important architecture or code structure issues
5. Medium-priority improvements

Rules:
- Do not add unnecessary new features.
- Do not over-engineer.
- Keep the system practical for a solo founder.
- Preserve the current architecture unless the review found a serious problem.
- Stay within the current phase.
- Update documentation if behavior or setup changes.
- Add or update tests for every important fix.
- Do not continue to the next phase.

Before editing:
1. Summarize the issues you will fix.
2. List files you plan to modify.
3. Confirm what you will not fix yet.
4. Produce a short implementation plan.

After editing:
1. Summarize what was fixed.
2. List files changed.
3. List tests added or updated.
4. List remaining known issues.
5. State whether the project is ready to rerun Prompt 10.
```
