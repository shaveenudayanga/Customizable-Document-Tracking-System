-- Ensure unique constraint per-tenant on email (for schema with column-level tenant)
-- Postgres-safe DDL; if existing global unique exists, drop it first safely.
DO $$
BEGIN
  -- Drop existing unique on email if present (created by JPA ddl-auto earlier)
  IF EXISTS (
    SELECT 1 FROM pg_indexes WHERE schemaname = current_schema() AND indexname = 'uk_users_email'
  ) THEN
    EXECUTE 'DROP INDEX uk_users_email';
  END IF;
EXCEPTION WHEN undefined_table THEN
  -- table may not exist yet; skip
  NULL;
END$$;

-- Create a unique index on (tenant, email)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_tenant_email ON users (tenant, email);
