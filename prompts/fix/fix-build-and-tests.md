# Fix Prompt — Build and Test Failures

```text
You are a senior engineer debugging build and test failures.

Read CLAUDE.md first.

Goal:
Fix current build and test failures without adding new features.

Rules:
1. Run or inspect the failing command.
2. Identify the root cause.
3. Make the smallest safe fix.
4. Do not skip tests to make the build pass.
5. Do not delete important tests unless they are clearly wrong.
6. Do not implement future phase features.
7. Update docs only if setup instructions changed.

Before editing:
1. Summarize the failing command or error.
2. Explain likely root cause.
3. List files to inspect or modify.
4. Produce a short fix plan.

After editing:
1. Summarize the fix.
2. List files changed.
3. Show the command to verify.
4. Mention any remaining issues.
```
