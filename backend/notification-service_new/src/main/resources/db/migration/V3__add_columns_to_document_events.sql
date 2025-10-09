-- Add extra columns to store richer event data
ALTER TABLE IF EXISTS document_events
  ADD COLUMN IF NOT EXISTS event_type varchar(100),
  ADD COLUMN IF NOT EXISTS document_id varchar(255),
  ADD COLUMN IF NOT EXISTS data text;
