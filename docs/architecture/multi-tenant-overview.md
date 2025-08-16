# Multi-Tenant Document Tracking System — Architecture & Operations Overview

Goal: Give any engineer/PM/LLM a complete, working mental model of the product—what it does, how it’s structured, how it’s secured, and how it’s built and run—so we can confidently generate code, docs, and proposals.

---

## 1) Purpose & Value Proposition

• Problem: Physical documents are slow, go missing, and provide poor visibility.
• Solution: A SaaS platform that digitizes documents and tracks their journey through configurable, company-specific workflows with real-time status, role-based actions, audits, QR/hash verification, and notifications.
• Tenancy: Each company is isolated by subdomain ({company}.domain.com) and PostgreSQL schema-per-tenant.
• Outcome: Faster processing, fewer lost docs, full chain-of-custody, and actionable analytics.

---

## 2) Actors, Roles & Responsibilities

| Role | Primary Goals | Capabilities (high-level) |
| --- | --- | --- |
| Admin (per company) | Configure and govern | Tenant setup, pipeline/route design, user & agent management, roles & permissions, global monitoring, analytics & reporting, notification rules, full audit access. |
| Handover Agent | Move documents through assigned steps | View personal work queue, scan/enter QR/hash to “receive/advance” docs at specific workflow steps, add notes, attach evidence. Only within assigned stages. |
| End User | Submit & track documents | Create/submit docs into a pipeline, upload attachments, see live status & ETA, receive notifications, verify via QR/hash. |

RBAC Principle: Least privilege; UI and APIs reflect role capabilities exactly.

---

## 3) Core Concepts & Business Logic

• Tenant (Organization): A subscribed company with its own subdomain and DB schema. Admins belong to exactly one tenant.
• Pipeline/Workflow: A company-defined route of ordered steps (stages) with rules (assignees, SLAs, prerequisites, required fields, allowed transitions).
• Document: A tracked item with metadata, attachments, current step, history, and a globally unique, tenant-scoped hash/QR for verification & handover.
• Handover Event: A signed, timestamped state transition (receive/advance/reject/return), typically performed by a Handover Agent, producing a durable audit entry.
• Notifications: Configurable triggers (e.g., document stuck, SLA breach, transition completed) to channels (email/SMS/push/in-app).
• Audit: Immutable, append-only trail of every read/write/transition, filterable/exportable (company scope).

---

## 4) Key Workflows (E2E)

### A. Self-Service Tenant Onboarding

1. Company signs up → creates Admin user.
2. Payment collected (subscription plan).
3. System provisions DB schema for tenant, seeds defaults, binds subdomain.
4. Admin logs in at {company}.domain.com to configure pipelines, users, agents, notification policies.

### B. Pipeline Design (per Tenant)

• Admin defines steps, rules, SLAs, required fields, assignees, allowed transitions, and handover requirements (QR only, manual allowed, both).
• Version pipelines (draft → active). Migration rules for in-flight docs.

### C. Document Submission (End User)

1. User selects pipeline, fills metadata, uploads attachments.
2. System issues Document ID + tenant-scoped hash; generates QR.
3. Document enters initial step; notifications sent per policy.

### D. Handover / Status Advancement (Agent)

1. Agent opens their queue (assigned steps).
2. Scans QR or enters hash → system validates tenant, role, step permission, document state.
3. Agent records action (receive/advance/reject/return), adds notes/attachments.
4. Handover event persisted; audit updated; notifications sent.

### E. Stuck/SLA Management

• Background scheduler detects stuck docs or SLA breaches (e.g., no progress within X hours).
• Triggers notifications to Admin/agents; optional auto-escalation (assign to supervisor, bump priority).

### F. Verification & Audit

• Anyone with the QR/hash (and appropriate tenant context) can verify authenticity & status.
• Admin can export audit logs, status histories, and analytics.

---

## 5) Technical Stack & Architectural Principles

• Frontend: React (SPA), subdomain-aware routing; secure calls to backend; QR scanning support.
• Backend: Spring Boot microservices (Java); services are independently deployable.
• Data: PostgreSQL with schema-per-tenant isolation; automatic schema provisioning/migration.
• AuthN/Z: JWT with tenant claim; RBAC enforced at gateway + service layers.
• DevOps: Dockerized services; Kubernetes for orchestration; CI/CD for build/test/deploy.
• Monorepo: /backend/{user,document,pipeline,handover,notification,audit,organization}, /frontend.

Service Boundaries (suggested):

• Organization Service: Tenants, subdomains, billing hooks, schema lifecycle.
• User Service: Users, roles, groups, auth, session/jwt issuance (with tenant claim).
• Pipeline Service: Workflow definitions, versions, validation, SLA rules.
• Document Service: Document CRUD, metadata, attachments (store pointers), current status.
• Handover Service: QR/hash verification, state transitions, chain-of-custody.
• Notification Service: Templates, channels, delivery & retries, tenant configs.
• Audit Service: Append-only audit store, queries/exports, compliance reports.
• Optional infra: API Gateway, Edge Auth, File Storage abstraction.

Inter-Service Communication: Primarily REST; consider async events (Kafka/SQS) for notifications, audit, and analytics to decouple write paths and avoid cross-service latency coupling.

---

## 6) Multi-Tenancy & Isolation

• Routing: {tenant}.domain.com → Gateway attaches tenant context.
• DB Isolation: One PostgreSQL schema per tenant; strong logical separation; per-schema migrations.
• JWT: Includes tenant_id, roles, scopes, exp. All service calls require tenant match between subdomain, JWT, and data access path.
• Code Path Guards: Multi-layer checks—gateway, service filters, and repository query scopes—to prevent cross-tenant access.
• Provisioning: On onboarding, automatically create schema, seed tables, and default roles; idempotent and retry-safe.

---

## 7) Security Model

Authentication & Session
• OAuth2/JWT; short-lived access tokens + refresh; tokens are tenant-bound.
• Optional SSO (SAML/OIDC) per tenant (enterprise plans).

Authorization (RBAC)
• Roles: ADMIN, AGENT, USER (+ optional granular permissions).
• Enforce in backend and reflected in UI. Policy engine checks: allow(action, resource, tenant, role).

Data Protection
• TLS everywhere; encryption at rest (DB/storage).
• Secrets in a secure vault (e.g., KMS/HashiCorp Vault).
• Input validation and canonicalization for all QR/hash and user input.

QR/Hash Verification
• QR encodes {tenant_id}.{document_id}.{nonce}.{signature}.
• Signature (HMAC/ECDSA) prevents tampering; server verifies signature + tenant match + doc state.
• Rate limiting on verification endpoints; replay protection via nonce/exp.

Audit & Compliance
• Immutable audit log (append-only), time-synced (NTP), actor, action, before/after deltas, origin IP, user agent.
• Export with redaction; support legal holds.

Operational Security
• Principle of least privilege, code owners, protected branches, dependency scanning, SAST/DAST, container image scanning.
• Regular third-party pentests; incident response runbooks.

---

## 8) Data Model (high-level sketch)

• tenant: tenant_id, name, subdomain, plan, status, created_at
• user: user_id, tenant_id, email, name, role, status, last_login
• pipeline: pipeline_id, tenant_id, name, version, is_active, definition_json
• pipeline_step: step_id, pipeline_id, name, order, assignee_group, sla_hours, required_fields
• document: doc_id, tenant_id, pipeline_id, current_step_id, hash, qr_url, created_by, priority, status
• document_metadata: doc_id, key, value (JSONB)
• attachment: attachment_id, doc_id, storage_uri, checksum, created_at
• handover_event: event_id, doc_id, from_step, to_step, agent_id, note, proof_uri, timestamp, signature
• notification_rule: rule_id, tenant_id, trigger, channel, template_id, recipients
• notification_log: notif_id, rule_id, doc_id, status, attempts, last_error
• audit_log: audit_id, tenant_id, actor, action, resource, payload, ip, ua, ts

All tables live per tenant schema; global control plane tables (e.g., tenant registry, subdomain map) live in a provider schema.

---

## 9) API Design (selected examples)

Auth
• POST /auth/login → JWT (claims: sub, tenant_id, roles, exp)
• POST /auth/refresh

Tenancy (control plane)
• POST /tenants (provision schema + subdomain)
• GET /tenants/{id}

Pipelines
• POST /pipelines (Admin)
• PUT /pipelines/{id}/activate
• GET /pipelines?active=true

Documents
• POST /documents (User)
• GET /documents/{id}
• GET /documents?step=...&status=...&priority=... (filtering)
• POST /documents/{id}/attachments
• GET /documents/{id}/qr (returns signed PNG/SVG)

Handover
• POST /handover/verify (qr/hash) → doc summary + allowed actions
• POST /handover/{docId}/advance (Agent)
• POST /handover/{docId}/reject / return

Notifications
• POST /notifications/rules
• GET /notifications/logs?status=failed

Audit
• GET /audit?actor=...&action=...&from=...&to=...
• POST /audit/export

Conventions
• REST+JSON, 201/202 semantics, idempotency keys for transition endpoints.
• Tenant enforcement via subdomain + JWT claim; requests rejected if mismatch.

---

## 10) Frontend UX & Accessibility

General
• Subdomain-aware login; role-based navigation.
• Global search & filters; saved views; column configuration.
• Timezone-aware timestamps; localization-ready labels.

Admin
• Pipeline designer (drag-and-drop steps, rules, SLAs).
• User & role management, assignment of Agents to steps.
• Real-time dashboards: throughput, average time per step, stuck docs, SLA breaches, agent workload.
• Notification rule builder (when/if/then).

Handover Agent
• My Queue list with SLA timers and priority badges.
• QR scanner (camera access) + offline fallback (hash manual entry).
• One-click actions (receive/advance/reject); attach notes/photos.

End User
• Simple submission form with validations and progress bar.
• Track status page with timeline and ETA.
• Opt-in notification preferences.

Accessibility
• Keyboard-first navigation, ARIA roles, contrast/zoom, screen-reader support.
• Mobile-friendly scanning for Agents.

---

## 11) Deployment & Operations

Environments: dev → staging → prod, with feature flags for progressive rollout.

CI/CD (monorepo)
1. Lint/format (frontend/backend).
2. Unit tests (coverage gates).
3. SAST/dependency scan.
4. Build Docker images; tag by commit SHA.
5. Integration/contract tests (spin ephemeral env).
6. Apply DB migrations (safe/forward-only) per tenant schema (batched/rolling).
7. Deploy to K8s (blue/green or canary).
8. Post-deploy smoke tests.

Kubernetes
• HPA for stateless services; resource limits.
• PgBouncer for connection pooling (many schemas).
• Centralized logging (ELK/OpenSearch), metrics (Prometheus), tracing (OTel).

Backups & DR
• Point-in-time recovery for Postgres; scheduled snapshots.
• Cross-region replicas; tested restore runbooks.
• Versioned object storage for attachments.

Observability & SLOs
• SLOs: Availability (e.g., 99.9%), Transition latency (p95), Notification delivery success.
• Alerting on tenant-scoped errors, SLA breach spikes, queue backlogs.

---

## 12) Scalability & Performance Considerations

• Schema-per-tenant scales well for isolation, with trade-offs in schema count; mitigate via:
  – Connection pooling, statement caching, template schema for fast provisioning, and archiving inactive tenants.
• Hot paths: Handover verification & transition—optimize with:
  – Precomputed current-state doc row, GIN indexes on (tenant, status, step, priority), read-through cache for pipeline definitions.
• Async workloads: Notifications, analytics, and audit writes via background workers/queues.
• File handling: Stream uploads; store only URIs/checksums in DB.

---

## 13) Edge Cases & Failure Modes (with mitigations)

• Privilege escalation: Enforce tenant/role checks at gateway + service + DB query scopes; comprehensive authorization tests.
• Cross-tenant leakage: Mandatory tenant match (subdomain ↔ JWT ↔ resource’s tenant_id); add deny-by-default guards.
• Stuck workflows: Scheduled detectors + escalation paths; admin override tools.
• Conflicting pipeline versions: Freeze definition at doc creation; on activation, new docs use vN+1; provide migration rules for in-flight docs if needed.
• QR replay/tampering: Signed payload with exp/nonce; one-time action tokens for critical transitions.
• Mass notification failures: Dead-letter queues, exponential backoff, provider failover, rate caps per tenant.
• Burst onboarding: Pre-warm schema templates; async provisioning with status; queueing and human-in-the-loop if billing check fails.
• Large attachments: Size/type limits, antivirus scan hooks, content hashing, resumable uploads.

---

## 14) Testing & Quality Strategy

• Unit tests: Services, validators, RBAC policy engine.
• Contract tests: API schemas (OpenAPI), consumer-driven contracts between services.
• Integration tests: End-to-end flows: onboarding → pipeline → submit → handover → notify → audit.
• Security tests: AuthZ matrix, JWT tamper tests, multi-tenant isolation tests, SSRF/XSS/Injection scans.
• Performance tests: Handover p95, notification throughput, onboarding throughput.
• Data migration tests: Per-schema idempotent migrations with roll-forward.

---

## 15) Developer Workflow & Collaboration

• Monorepo conventions:
  – /backend/{service}/src/main/java
  – /frontend/src
  – /infra (helm charts/kustomize, DB migration scripts)
• Branches: main (protected), feature branches → PRs with code review & checks.
• Code owners: Enforced for services and shared libs.
• Automation: Conventional commits, semantic versioning, changelog generation.
• Docs: ADRs for architectural decisions; service READMEs; runbooks; OpenAPI sources of truth.

---

## 16) Security & Compliance Posture (operational)

• RBAC everywhere (K8s, cloud, DB).
• Rotation: Keys/tokens rotated, short-lived creds.
• Least data: Store only needed PII; encrypt sensitive metadata; per-tenant data retention policies.
• Compliance-ready: Audit exports, DPA, data residency (via regional clusters if needed), incident response SLAs.

---

## 17) Analytics & Reporting

• Company dashboards: Throughput, average time per step, bottlenecks, on-time rate vs SLA, rework loops.
• Agent performance: Completed handovers, p95 handling time, breach counts.
• Operational analytics: Notification success, queue latencies, error rates.
• Data pipeline: Events → queue → analytics store (could be Postgres OLAP schema or external warehouse) with per-tenant isolation.

---

## 18) “Big Picture” — How It All Fits Together

1. Tenant onboarding creates a dedicated schema and subdomain; the Organization Service is the control plane.
2. Admins design pipelines; definitions are cached and versioned by the Pipeline Service.
3. Users submit documents; the Document Service issues a QR/hash and holds current state.
4. Agents perform handover transitions using QR; the Handover Service validates tenant/role/step and appends audit.
5. Notifications fire via async workers; Audit Service captures every action; Analytics aggregates per tenant.
6. Security is enforced at the edge (gateway), within every service, and via DB scoping—aligned to JWT tenant claims.
7. DevOps automates build/test/deploy, migrates each tenant schema, and monitors SLOs; K8s scales stateless services and keeps costs predictable.

---

## 19) Ready-to-Build Notes (pragmatic defaults)

• Migrations: Flyway/Liquibase per schema; template schema for fast provisioning.
• Storage: Postgres for metadata; object storage (S3-compatible) for attachments.
• Queues: Start with a managed queue (e.g., SQS/Kafka) for notifications/audit.
• Gateway: Spring Cloud Gateway or NGINX Ingress with subdomain → tenant resolver.
• JWT: Include tenant_id, roles, scopes, and pipeline_permissions (optional); sign with rotating keys.
• Caching: Pipeline definitions in Redis (tenant-scoped); short TTL + cache bust on publish.
• Rate limiting: Per tenant and per user; stricter on verification endpoints.
• Feature flags: Per tenant rollout for new workflow features.

---

## 20) Risks & Mitigations (snapshot)

• Many schemas → connection overhead: Use PgBouncer; cap max connections; shard tenants across DB instances if needed.
• Misconfigured pipelines → dead-ends: Designer validates reachability; pre-publish simulation; guardrails (must have terminal state).
• Data leakage via logs/exports: Structured logging with PII redaction; export access logged & permissioned.
• Notification provider outage: Multi-provider abstraction + retry/backoff + circuit breakers.

---

### TL;DR

Subdomain-routed, schema-per-tenant, Spring Boot microservices where Admins design pipelines, Users submit documents, and Agents move them with QR-verified handovers. Everything is wrapped in JWT-based RBAC, full auditability, notifications, and Kubernetes-backed CI/CD. The design prioritizes strong tenant isolation, operational rigor, and extensibility.
