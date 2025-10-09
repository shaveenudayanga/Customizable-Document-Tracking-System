-- Database Verification Queries for Notification Service
-- Use these queries to verify the database setup and data persistence

-- ============================================
-- 1. SCHEMA VERIFICATION
-- ============================================

-- Check if document_events table exists
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'document_events'
) AS table_exists;

-- View table structure
SELECT 
    column_name, 
    data_type, 
    character_maximum_length, 
    is_nullable, 
    column_default
FROM information_schema.columns
WHERE table_name = 'document_events'
ORDER BY ordinal_position;

-- Check indexes
SELECT 
    indexname, 
    indexdef
FROM pg_indexes
WHERE tablename = 'document_events';

-- Check sequence
SELECT * FROM document_event_id_seq;

-- ============================================
-- 2. DATA VERIFICATION
-- ============================================

-- Count total events
SELECT COUNT(*) AS total_events FROM document_events;

-- Count by event type
SELECT 
    event_type, 
    COUNT(*) AS count
FROM document_events
GROUP BY event_type
ORDER BY count DESC;

-- Recent events (last 20)
SELECT 
    id,
    event_id,
    event_type,
    document_id,
    LEFT(data, 100) AS data_snippet,
    created_at,
    updated_at
FROM document_events
ORDER BY created_at DESC
LIMIT 20;

-- Events by document
SELECT 
    document_id,
    COUNT(*) AS event_count,
    MIN(created_at) AS first_event,
    MAX(created_at) AS last_event
FROM document_events
WHERE document_id IS NOT NULL
GROUP BY document_id
ORDER BY event_count DESC;

-- ============================================
-- 3. DATA QUALITY CHECKS
-- ============================================

-- Check for NULL event_ids (should be 0)
SELECT COUNT(*) AS null_event_ids
FROM document_events
WHERE event_id IS NULL;

-- Check for duplicate event_ids (should be 0)
SELECT 
    event_id, 
    COUNT(*) AS duplicate_count
FROM document_events
GROUP BY event_id
HAVING COUNT(*) > 1;

-- Check for events without timestamps
SELECT COUNT(*) AS missing_created_at
FROM document_events
WHERE created_at IS NULL;

-- ============================================
-- 4. SAMPLE QUERIES FOR SPECIFIC EVENT TYPES
-- ============================================

-- Document creation events
SELECT 
    id,
    event_id,
    document_id,
    data,
    created_at
FROM document_events
WHERE event_type = 'document.created'
ORDER BY created_at DESC
LIMIT 10;

-- Workflow events
SELECT 
    id,
    event_id,
    event_type,
    document_id,
    created_at
FROM document_events
WHERE event_type LIKE 'workflow.%'
ORDER BY created_at DESC
LIMIT 10;

-- ============================================
-- 5. PERFORMANCE CHECKS
-- ============================================

-- Check table size
SELECT 
    pg_size_pretty(pg_total_relation_size('document_events')) AS total_size,
    pg_size_pretty(pg_relation_size('document_events')) AS table_size,
    pg_size_pretty(pg_indexes_size('document_events')) AS indexes_size;

-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan AS index_scans,
    idx_tup_read AS tuples_read,
    idx_tup_fetch AS tuples_fetched
FROM pg_stat_user_indexes
WHERE tablename = 'document_events'
ORDER BY idx_scan DESC;

-- ============================================
-- 6. SEARCH BY SPECIFIC CRITERIA
-- ============================================

-- Find events by event_id (replace 'evt-12345' with actual event_id)
-- SELECT * FROM document_events WHERE event_id = 'evt-12345';

-- Find events by document_id (replace 'DOC-123' with actual document_id)
-- SELECT * FROM document_events WHERE document_id = 'DOC-123' ORDER BY created_at;

-- Find events within date range
-- SELECT * FROM document_events 
-- WHERE created_at BETWEEN '2024-01-01' AND '2024-12-31'
-- ORDER BY created_at DESC;

-- ============================================
-- 7. QUICK HEALTH CHECK (run this first)
-- ============================================

SELECT 
    'Table Exists' AS check_name,
    CASE WHEN EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_name = 'document_events'
    ) THEN 'PASS' ELSE 'FAIL' END AS status
UNION ALL
SELECT 
    'Has Events',
    CASE WHEN EXISTS (SELECT 1 FROM document_events) 
    THEN 'PASS (Count: ' || (SELECT COUNT(*)::text FROM document_events) || ')' 
    ELSE 'WARN (Empty)' END
UNION ALL
SELECT 
    'No Duplicate Event IDs',
    CASE WHEN NOT EXISTS (
        SELECT event_id FROM document_events 
        GROUP BY event_id HAVING COUNT(*) > 1
    ) THEN 'PASS' ELSE 'FAIL' END
UNION ALL
SELECT 
    'All Events Have Timestamps',
    CASE WHEN NOT EXISTS (
        SELECT 1 FROM document_events WHERE created_at IS NULL
    ) THEN 'PASS' ELSE 'FAIL' END;
