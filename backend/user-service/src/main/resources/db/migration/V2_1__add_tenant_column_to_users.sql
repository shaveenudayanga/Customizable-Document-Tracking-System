-- Ensure users.tenant column exists before creating per-tenant unique index in V3
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'users'
      AND column_name = 'tenant'
  ) THEN
    ALTER TABLE users ADD COLUMN tenant TEXT;
  END IF;
END$$;
