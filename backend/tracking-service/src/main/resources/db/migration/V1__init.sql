-- Tracking events for document movement
CREATE TABLE tracking_event (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    location VARCHAR(100),
    scanned_by VARCHAR(100),
    notes TEXT,
    qr_code VARCHAR(255),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast lookups
CREATE INDEX idx_tracking_document ON tracking_event(document_id);
CREATE INDEX idx_tracking_created ON tracking_event(created_at DESC);
