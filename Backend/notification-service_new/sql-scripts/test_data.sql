-- Test Data for Notification Service
-- Use these INSERT statements to test database connectivity and data persistence

-- ============================================
-- CLEAN UP TEST DATA (optional - run before inserting)
-- ============================================
-- DELETE FROM document_events WHERE event_id LIKE 'test-%';

-- ============================================
-- INSERT TEST DOCUMENT EVENTS
-- ============================================

-- Test 1: Document Created Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-doc-created-001',
    'document.created',
    'DOC-TEST-001',
    '{"documentId":"DOC-TEST-001","title":"Test Document","creator":"testuser","ownerEmail":"owner@example.com","eventId":"test-doc-created-001"}',
    CURRENT_TIMESTAMP
);

-- Test 2: Document Updated Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-doc-updated-001',
    'document.updated',
    'DOC-TEST-001',
    '{"documentId":"DOC-TEST-001","title":"Test Document v2","updater":"testuser2","ownerEmail":"owner@example.com","eventId":"test-doc-updated-001"}',
    CURRENT_TIMESTAMP
);

-- Test 3: Document Approved Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-doc-approved-001',
    'document.approved',
    'DOC-TEST-001',
    '{"documentId":"DOC-TEST-001","approver":"manager","ownerEmail":"owner@example.com","eventId":"test-doc-approved-001"}',
    CURRENT_TIMESTAMP
);

-- Test 4: Document Rejected Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-doc-rejected-001',
    'document.rejected',
    'DOC-TEST-002',
    '{"documentId":"DOC-TEST-002","rejector":"reviewer","reason":"Incomplete information","ownerEmail":"owner@example.com","eventId":"test-doc-rejected-001"}',
    CURRENT_TIMESTAMP
);

-- Test 5: Document Error Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-doc-error-001',
    'document.error',
    'DOC-TEST-003',
    '{"documentId":"DOC-TEST-003","reason":"Processing failed","eventId":"test-doc-error-001"}',
    CURRENT_TIMESTAMP
);

-- ============================================
-- INSERT TEST WORKFLOW EVENTS
-- ============================================

-- Test 6: Workflow Started Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-workflow-started-001',
    'workflow.started',
    'DOC-TEST-004',
    '{"documentId":"DOC-TEST-004","pipelineInstanceId":"100","processInstanceId":"proc-1","initiator":"testuser","ownerEmail":"owner@example.com","eventId":"test-workflow-started-001"}',
    CURRENT_TIMESTAMP
);

-- Test 7: Task Completed Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-task-completed-001',
    'workflow.task.completed',
    'DOC-TEST-004',
    '{"documentId":"DOC-TEST-004","pipelineInstanceId":"100","processInstanceId":"proc-1","taskId":"task-1","taskName":"Review","approved":"true","completedBy":"reviewer","notes":"Looks good","ownerEmail":"owner@example.com","eventId":"test-task-completed-001"}',
    CURRENT_TIMESTAMP
);

-- Test 8: Workflow Completed Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-workflow-completed-001',
    'workflow.completed',
    'DOC-TEST-004',
    '{"documentId":"DOC-TEST-004","pipelineInstanceId":"100","processInstanceId":"proc-1","completedBy":"system","ownerEmail":"owner@example.com","eventId":"test-workflow-completed-001"}',
    CURRENT_TIMESTAMP
);

-- Test 9: Workflow Rejected Event
INSERT INTO document_events (event_id, event_type, document_id, data, created_at)
VALUES (
    'test-workflow-rejected-001',
    'workflow.rejected',
    'DOC-TEST-005',
    '{"documentId":"DOC-TEST-005","pipelineInstanceId":"101","processInstanceId":"proc-2","rejectedBy":"reviewer","ownerEmail":"owner@example.com","eventId":"test-workflow-rejected-001"}',
    CURRENT_TIMESTAMP
);

-- ============================================
-- VERIFY TEST DATA
-- ============================================

-- Count test events
SELECT 
    'Total Test Events' AS description,
    COUNT(*) AS count
FROM document_events
WHERE event_id LIKE 'test-%';

-- View all test events
SELECT 
    id,
    event_id,
    event_type,
    document_id,
    LEFT(data, 80) AS data_snippet,
    created_at
FROM document_events
WHERE event_id LIKE 'test-%'
ORDER BY created_at DESC;

-- ============================================
-- CLEANUP TEST DATA (run after testing)
-- ============================================

-- Uncomment the following line to delete all test data:
-- DELETE FROM document_events WHERE event_id LIKE 'test-%';
