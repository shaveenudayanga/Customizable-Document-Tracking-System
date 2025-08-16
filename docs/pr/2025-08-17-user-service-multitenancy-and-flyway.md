# PR: User Service — Multi-Tenancy Enforcement, Login Enforcement (flags), and Flyway migration

## Summary
Implements tenant context and enforcement (feature-flagged), optional login-time enforcement, and tenant-aware data access for admin/user endpoints. Adds Flyway and a Postgres-safe unique index on `(tenant, email)`. All unit and integration tests pass.

## Changes
- Multi-tenancy
  - Tenant resolution from subdomain via filter; request-scoped TenantContext.
  - Feature-flagged request enforcement comparing Host/JWT/User tenant → 403 on mismatch.
  - Optional login-time enforcement (blocks login on mismatched Host vs. user.tenant).
  - JWT access tokens include tenant claim.
- Data model & repo
  - `users.tenant` column and tenant-scoped repository methods (read/update/delete/list).
  - Admin operations restricted to current tenant when TenantContext present.
- Error semantics
  - `UsernameNotFoundException` mapped to 401 Unauthorized (was 500).
- Config
  - Fixed path: `security.tenant.login-enforce` now under `security.tenant`.
  - Added Flyway with prod profile enabled.
- Migration (Flyway V1)
  - Drops legacy global unique index on `users.email` if present.
  - Creates unique index `ux_users_tenant_email` on `(tenant, email)`.
- Tests: All green (unit + integration) including enforcement, admin scoping, refresh, and login enforcement.

## Flags (env vars)
- `TENANT_ENFORCE` (default false): request-time enforcement (Host/JWT/User).
- `TENANT_LOGIN_ENFORCE` (default false): login-time enforcement (Host vs. user.tenant).
- `JWT_SECRET` (required in non-dev): HS256 secret; rotate via env/secret manager.

application.yml wiring
- Default profile: Flyway disabled; H2 for tests, Postgres locally.
- Prod profile: Flyway enabled; Postgres URL `jdbc:postgresql://db:5432/docutrace_users`.

## Recommended environment settings
- Local/dev:
  - `TENANT_ENFORCE=false`
  - `TENANT_LOGIN_ENFORCE=false`
  - `JWT_SECRET=dev-only-long-secret`
- Staging:
  - `TENANT_ENFORCE=true`
  - `TENANT_LOGIN_ENFORCE=false` (start off; enable later if desired)
  - `JWT_SECRET=<staging-secret>`
- Production:
  - `TENANT_ENFORCE=true`
  - `TENANT_LOGIN_ENFORCE=true` (optional; requires Host/subdomain correctness)
  - `JWT_SECRET=<prod-secret>`

## Deployment order
1) Apply DB migrations (Flyway runs automatically in prod profile at boot).
2) Deploy user-service with `TENANT_ENFORCE=true` in staging → run smoke tests.
3) Optionally enable `TENANT_LOGIN_ENFORCE=true` after confirming Host routing/subdomains.

## Rollback plan
- If issues arise after enabling flags:
  - Set `TENANT_ENFORCE=false` and/or `TENANT_LOGIN_ENFORCE=false` to temporarily disable enforcement.
  - Flyway index change is additive and safe to keep.

## Validation
- Tests: green on branch `feature/user-service`.
- Manual checks (staging):
  - Register at `acme.domain` then attempt login at `other.domain` → 401 when login enforcement on.
  - Access admin list with token issued for `acme` from `other` Host → 403 when enforcement on.

## Security notes
- JWT must carry tenant claim; enforcement validates alignment across layers.
- Ensure wildcard ingress and DNS reflect subdomain → tenant mapping correctly prior to enabling login enforcement.

## Files of interest
- `backend/user-service/src/main/resources/application.yml` (flags + Flyway)
- `backend/user-service/src/main/resources/db/migration/V1__users_tenant_email_unique_index.sql`
- Security/tenant filters, services, tests under `backend/user-service/src/main/java` and `/test/java`.

## How to run tests locally (optional)
```bash
mvn -q -f backend/user-service/pom.xml -DskipITs=false test
```

## Next follow-ups (separate PRs)
- Move from Hibernate ddl-auto to Flyway-managed schema fully (ddl-auto=none).
- Add DB index for frequent admin queries per tenant if needed.
- Bump to Java 21 toolchain.
