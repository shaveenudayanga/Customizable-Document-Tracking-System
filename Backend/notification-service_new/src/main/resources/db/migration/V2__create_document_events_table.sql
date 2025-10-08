-- Flyway migration: create document_events table
CREATE SEQUENCE IF NOT EXISTS document_event_id_seq;

CREATE TABLE IF NOT EXISTS document_events (
  id bigint PRIMARY KEY DEFAULT nextval('document_event_id_seq'),
  event_id varchar(255) NOT NULL UNIQUE,
  created_at timestamp NOT NULL DEFAULT now(),
  updated_at timestamp
);
