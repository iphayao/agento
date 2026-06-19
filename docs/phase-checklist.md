# Phase Completion Checklist

Before moving to the next phase, confirm:

## Code

- [ ] Feature works locally
- [ ] Code follows current architecture
- [ ] No future phase was implemented early
- [ ] No hard-coded secrets
- [ ] Error handling exists
- [ ] Validation exists
- [ ] Database migration exists if schema changed

## Tests

- [ ] Backend unit tests added or updated
- [ ] Integration tests added where useful
- [ ] Frontend tests added where useful
- [ ] Agento-worker tests added where useful
- [ ] Existing tests pass

## Documentation

- [ ] README updated if setup changed
- [ ] API examples updated
- [ ] Environment variables documented
- [ ] Known limitations documented

## AI Safety

- [ ] AI output is validated before saving
- [ ] Generated content remains DRAFT
- [ ] Risky claims are checked
- [ ] API keys are not exposed to frontend

## Git

- [ ] Changes are reviewed
- [ ] Commit message is clear
- [ ] Phase is committed separately
