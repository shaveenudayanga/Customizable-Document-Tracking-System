-- Provider schema and tenant template baseline
CREATE SCHEMA IF NOT EXISTS provider;

CREATE TABLE IF NOT EXISTS provider.tenants (
  tenant_id UUID PRIMARY KEY,
  subdomain TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  plan TEXT NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('provisioning','active','suspended','deleted')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS provider.jwk_keys (
  key_id TEXT PRIMARY KEY,
  tenant_id UUID NULL,
  alg TEXT NOT NULL,
  "use" TEXT NOT NULL CHECK ("use"='sig'),
  material JSONB NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  rotated_at TIMESTAMPTZ
);

-- tenant template objects will be created per-tenant by provisioning process; included here as reference
