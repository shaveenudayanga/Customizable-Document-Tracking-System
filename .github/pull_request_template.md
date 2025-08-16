# PR Title

## Summary
- What changed and why.

## Key Changes
- Security/Tenant:
  - ...
- Data Model/Repo:
  - ...
- Config:
  - ...
- Migrations:
  - ...

## Flags / Config
- TENANT_ENFORCE: true|false
- TENANT_LOGIN_ENFORCE: true|false
- JWT_SECRET: <set in env/secret manager>

## Deployment & Rollout
- Order of operations:
  1. Apply DB migrations (Flyway)
  2. Deploy service with flags
  3. Verify smoke tests
- Rollback plan:
  - Toggle enforcement flags off if needed

## Validation
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual validation steps described

## Risks
- Impact if flags are misconfigured
- Backward-compat considerations

## Files of Interest
- application.yml
- db/migration/*
- security/tenant filters or related services/tests

## Follow-ups (optional)
- Future improvements or separate PRs
