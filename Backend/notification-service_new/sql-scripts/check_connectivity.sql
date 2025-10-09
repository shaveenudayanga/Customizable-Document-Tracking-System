-- Quick Database Connectivity and Health Check
-- Run this script first to verify database connection is working

-- ============================================
-- 1. CONNECTION TEST
-- ============================================

-- Check PostgreSQL version
SELECT version() AS postgres_version;

-- Check current database
SELECT current_database() AS current_db;

-- Check current user
SELECT current_user AS current_user;

-- Check current timestamp
SELECT CURRENT_TIMESTAMP AS server_time;

-- ============================================
-- 2. DATABASE OBJECTS CHECK
-- ============================================

-- List all tables in public schema
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- List all sequences
SELECT 
    sequence_name,
    last_value
FROM information_schema.sequences
WHERE sequence_schema = 'public'
ORDER BY sequence_name;

-- List all indexes on document_events
SELECT 
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'document_events'
ORDER BY indexname;

-- ============================================
-- 3. FLYWAY MIGRATION STATUS
-- ============================================

-- Check if flyway_schema_history exists
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_name = 'flyway_schema_history'
) AS flyway_table_exists;

-- List applied migrations (if Flyway is used)
SELECT 
    installed_rank,
    version,
    description,
    type,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank DESC;

-- ============================================
-- 4. QUICK FUNCTIONALITY TEST
-- ============================================

-- Test INSERT (using a unique test ID)
INSERT INTO document_events (event_id, event_type, document_id, data)
VALUES (
    'connectivity-test-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDDHH24MISS'),
    'test.connectivity',
    'TEST-CONN',
    '{"test": "connectivity check"}'
);

-- Test SELECT
SELECT 
    id,
    event_id,
    event_type,
    document_id,
    created_at
FROM document_events
WHERE event_type = 'test.connectivity'
ORDER BY created_at DESC
LIMIT 5;

-- Count all records
SELECT COUNT(*) AS total_records FROM document_events;

-- ============================================
-- 5. CLEANUP TEST RECORDS (optional)
-- ============================================

-- Uncomment to remove connectivity test records:
-- DELETE FROM document_events WHERE event_type = 'test.connectivity';

-- ============================================
-- 6. SUMMARY HEALTH CHECK
-- ============================================

SELECT 
    'Database Connection' AS check_type,
    'PASS' AS status,
    current_database() AS details
UNION ALL
SELECT 
    'User Permissions',
    CASE WHEN has_table_privilege('document_events', 'INSERT') 
    THEN 'PASS' ELSE 'FAIL' END,
    'Can insert: ' || has_table_privilege('document_events', 'INSERT')::text
UNION ALL
SELECT 
    'Table Exists',
    CASE WHEN EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_name = 'document_events'
    ) THEN 'PASS' ELSE 'FAIL' END,
    'document_events table'
UNION ALL
SELECT 
    'Sequence Exists',
    CASE WHEN EXISTS (
        SELECT FROM information_schema.sequences 
        WHERE sequence_name = 'document_event_id_seq'
    ) THEN 'PASS' ELSE 'FAIL' END,
    'document_event_id_seq'
UNION ALL
SELECT 
    'Data Present',
    CASE WHEN EXISTS (SELECT 1 FROM document_events LIMIT 1)
    THEN 'YES (' || (SELECT COUNT(*)::text FROM document_events) || ' records)'
    ELSE 'NO (empty table)' END,
    'Total records in table';
