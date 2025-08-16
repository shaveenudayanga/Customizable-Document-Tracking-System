# ADR 0002: Multi-Tenancy Approach (Subdomain + Schema-per-Tenant)

Date: 2025-08-16

## Status
Accepted

## Context
We are building a SaaS document tracking platform requiring strong tenant isolation while maintaining operational efficiency. Tenants need to be logically isolated with clear blast-radius boundaries, and we expect thousands of tenants of varying sizes.

## Decision
Adopt subdomain-based routing (`{tenant}.domain.com`) and PostgreSQL schema-per-tenant data isolation. JWTs will carry a required `tenant_id` claim and role claims. All inbound requests must satisfy a three-way tenant match: subdomain ↔ JWT.claims.tenant_id ↔ resource.tid.

Enforcement occurs at multiple layers:
- Edge/Gateway: Resolve subdomain to tenant and attach tenant context headers.
- Service filters: Validate tenant context against JWT; deny by default on mismatch.
- Repository/data: Scope queries by tenant schema and tenant id; never query across schemas.

## Consequences
Pros:
- Strong isolation with clear performance and operational boundaries.
- Simplifies per-tenant migrations and data retention.
- Enables tenant-level throttling, backup/restore, and DR.

Cons:
- Many schemas increase connection overhead and migration fan-out.
- Requires robust provisioning automation and pooling (PgBouncer).

Mitigations:
- Use template schemas for fast provisioning and idempotent migrations (Flyway/Liquibase).
- Connection pooling and statement caching; shard tenants across DB instances if necessary.
- Strict deny-by-default guards and tests for cross-tenant access.

## Alternatives Considered
- Single schema with tenant_id column partitioning: simpler DDL but weaker operational isolation; higher risk of accidental cross-tenant leaks.
- Separate databases per tenant: strongest isolation but high cost and management overhead; slower provisioning.

## References
- docs/architecture/multi-tenant-overview.md
- docs/adr/0001-user-ids-and-cross-service-references.md
