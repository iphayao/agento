# Commit Prompt — Create Recommended Commit

```text
You are a senior engineer preparing a clean git commit.

Read the current git diff.

Task:
1. Summarize the changes.
2. Identify the best conventional commit message.
3. If changes are too broad, recommend splitting into multiple commits.
4. Do not commit secrets.
5. Do not include generated local files, build outputs, or environment-specific files.

Suggested commit messages:
- chore: initialize agento repository structure
- docs: add soclean brand kit
- docs: add agento prompt library
- feat: add mvp content generator
- feat: add langgraph agent workflow
- feat: add brand memory rag
- feat: add performance learning
- feat: add content calendar
- feat: add content export
- chore: harden production readiness
- fix: address review issues

After checking the diff, provide:
1. Files included
2. Files excluded
3. Recommended commit message
4. Any risk before committing
```
