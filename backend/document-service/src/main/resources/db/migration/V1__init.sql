-- -----------------------------------------------------------------------------
-- Flyway Migration: V1__init
-- Purpose   : Create the "document" table to store metadata for tracked documents
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS document (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255)         NOT NULL,
    document_type   VARCHAR(100)         NOT NULL,
    description     TEXT,
    owner_user_id   UUID                 NOT NULL,
    status          VARCHAR(50)          NOT NULL,
    qr_path         VARCHAR(512),
    file_dir        VARCHAR(512),
    created_at      TIMESTAMPTZ          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ          NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Optional index to speed up lookups by owner and status
CREATE INDEX IF NOT EXISTS idx_document_owner_status
    ON document (owner_user_id, status);
