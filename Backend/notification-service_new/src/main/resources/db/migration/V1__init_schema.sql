-- Initial schema for notification service
-- Creates the document_events table with all required columns

CREATE SEQUENCE IF NOT EXISTS document_event_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS document_events (
    id BIGINT PRIMARY KEY DEFAULT nextval('document_event_id_seq'),
    event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100),
    document_id VARCHAR(255),
    data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_document_events_event_id ON document_events(event_id);
CREATE INDEX IF NOT EXISTS idx_document_events_event_type ON document_events(event_type);
CREATE INDEX IF NOT EXISTS idx_document_events_document_id ON document_events(document_id);
CREATE INDEX IF NOT EXISTS idx_document_events_created_at ON document_events(created_at DESC);

-- Add comments for documentation
COMMENT ON TABLE document_events IS 'Stores all document and workflow events for auditing and idempotency';
COMMENT ON COLUMN document_events.event_id IS 'Unique identifier for the event (for idempotency)';
COMMENT ON COLUMN document_events.event_type IS 'Type of event (e.g., document.created, workflow.started)';
COMMENT ON COLUMN document_events.document_id IS 'Reference to the document';
COMMENT ON COLUMN document_events.data IS 'JSON payload of the event';
COMMENT ON COLUMN document_events.created_at IS 'Timestamp when the event was first recorded';
COMMENT ON COLUMN document_events.updated_at IS 'Timestamp when the event was last updated';
