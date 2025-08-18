Multi‑Tenancy Architecture

- What it is
  - One platform serving many companies (tenants) safely on shared infrastructure.

- Tenant separation
  - Each tenant uses its own subdomain: {tenant}.domain.com.
  - Each tenant gets its own PostgreSQL schema (strong logical isolation).
  - A global control plane holds the tenant registry and subdomain map.

- Request flow and enforcement
  - Gateway resolves tenant from subdomain and attaches tenant context.
  - JWT includes tenant_id, roles, and scopes.
  - Every service checks: subdomain tenant == JWT tenant == data tenant before any read/write.

- Roles and permissions (RBAC)
  - Roles: ADMIN, AGENT, USER with least privilege.
  - UI and APIs expose only allowed actions per role and tenant.

- Core services (per-tenant aware)
  - Organization (tenants, billing, schema lifecycle), User (auth/roles), Pipeline, Document, Handover, Notification, Audit.
  - REST-first; async events for notifications, audit, analytics.

- Data and storage
  - Metadata in Postgres (schema per tenant); attachments in object storage (URIs in DB).
  - QR/hash embeds tenant-safe signed data; server verifies signature and state.

- Provisioning and migrations
  - On signup: create schema from template, seed defaults, bind subdomain.
  - Per-tenant, forward-only, idempotent migrations.

- Security
  - TLS everywhere; encryption at rest; secrets in a vault.
  - Multi-layer tenant guards (gateway, service filters, repository scopes).
  - Rate limits and replay protection on verification endpoints.
  - Full append-only audit per tenant with export and redaction.

- Operations and observability
  - Docker + Kubernetes; CI/CD with automated tests and smoke checks.
  - PgBouncer for many schemas; per-tenant logs/metrics/traces and SLOs.
  - Backups: PITR, snapshots, cross-region replicas; versioned object storage.

- Performance and scale
  - Indexes on (tenant, status, step, priority); read-through cache for pipeline definitions.
  - Hot paths (handover/verify) optimized; async queues for non-critical work.
  - Scale out services; shard tenants across DBs if needed.

- Risks and mitigations
  - Cross-tenant leakage → strict tenant matching and deny-by-default.
  - Schema count overhead → pooling, sharding, archiving inactive tenants.
  - Misconfigured workflows → validations, simulations, required terminal states.

- End result
  - Strong isolation, predictable operations, and scalable performance for many tenants on one platform.
