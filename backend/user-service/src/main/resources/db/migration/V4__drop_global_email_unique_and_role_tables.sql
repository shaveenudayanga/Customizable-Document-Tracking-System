-- Drop global unique on users.email and remove unused role tables; keep per-tenant unique (tenant,email)

DO $$
DECLARE
    cons RECORD;
BEGIN
    -- Drop any unique constraint on users(email) only
    FOR cons IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid AND t.relname = 'users'
        JOIN pg_namespace n ON n.oid = t.relnamespace AND n.nspname = current_schema()
        JOIN LATERAL (
            SELECT array_agg(att.attname ORDER BY att.attnum) AS cols
            FROM unnest(c.conkey) WITH ORDINALITY AS k(attnum, ord)
            JOIN pg_attribute att ON att.attrelid = c.conrelid AND att.attnum = k.attnum
        ) col ON true
        WHERE c.contype = 'u' AND col.cols = ARRAY['email']
    LOOP
        EXECUTE format('ALTER TABLE %I.%I DROP CONSTRAINT %I', current_schema(), 'users', cons.conname);
    END LOOP;
EXCEPTION WHEN others THEN
    -- ignore
    NULL;
END$$;

-- Drop non-unique email index if exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE schemaname = current_schema() AND indexname = 'idx_users_email'
    ) THEN
        EXECUTE 'DROP INDEX idx_users_email';
    END IF;
END$$;

-- Ensure unique (tenant,email)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_tenant_email ON users (tenant, email);

-- Drop unused role tables if present
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS role CASCADE;
