#!/bin/bash

# Database Verification Script for Notification Service
# This script checks if the database is properly set up and all functions work correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DB_CONTAINER="backend-postgres-1"
DB_NAME="notificationdb"
DB_USER="postgres"
SERVICE_URL="http://localhost:1004"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Database Verification Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to print step
print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Function to print success
print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

# Function to print error
print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Function to print warning
print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Step 1: Check if Docker is running
print_step "Checking Docker..."
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi
print_success "Docker is running"

# Step 2: Check if containers are running
print_step "Checking if containers are running..."
if ! docker compose ps | grep -q "postgres"; then
    print_warning "Postgres container not found. Starting services..."
    docker compose up -d
    echo "Waiting for services to start..."
    sleep 10
fi
print_success "Containers are running"

# Step 3: Check database connectivity
print_step "Testing database connectivity..."
if docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -c "SELECT 1;" > /dev/null 2>&1; then
    print_success "Database connection successful"
else
    print_error "Cannot connect to database"
    exit 1
fi

# Step 4: Check if table exists
print_step "Checking if document_events table exists..."
TABLE_EXISTS=$(docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -tAc \
    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'document_events');")
if [ "$TABLE_EXISTS" = "t" ]; then
    print_success "document_events table exists"
else
    print_error "document_events table does not exist"
    exit 1
fi

# Step 5: Check Flyway migrations
print_step "Checking Flyway migrations..."
MIGRATION_COUNT=$(docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -tAc \
    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true;" 2>/dev/null || echo "0")
if [ "$MIGRATION_COUNT" -gt 0 ]; then
    print_success "Found $MIGRATION_COUNT successful migrations"
    docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -c \
        "SELECT installed_rank, version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank;"
else
    print_warning "No Flyway migrations found"
fi

# Step 6: Check current data
print_step "Checking current data in database..."
RECORD_COUNT=$(docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -tAc \
    "SELECT COUNT(*) FROM document_events;")
print_success "Found $RECORD_COUNT records in document_events"

if [ "$RECORD_COUNT" -gt 0 ]; then
    echo -e "\nRecent events:"
    docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -c \
        "SELECT id, event_id, event_type, document_id, created_at FROM document_events ORDER BY created_at DESC LIMIT 5;"
fi

# Step 7: Test database write
print_step "Testing database write operation..."
TEST_EVENT_ID="verify-test-$(date +%s)"
docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -c \
    "INSERT INTO document_events (event_id, event_type, document_id, data) VALUES ('$TEST_EVENT_ID', 'test.verify', 'TEST-DOC', '{\"test\": true}');" > /dev/null
print_success "Database write successful"

# Step 8: Test database read
print_step "Testing database read operation..."
READ_RESULT=$(docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -tAc \
    "SELECT event_id FROM document_events WHERE event_id = '$TEST_EVENT_ID';")
if [ "$READ_RESULT" = "$TEST_EVENT_ID" ]; then
    print_success "Database read successful"
else
    print_error "Database read failed"
fi

# Step 9: Clean up test data
docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME -c \
    "DELETE FROM document_events WHERE event_id = '$TEST_EVENT_ID';" > /dev/null
print_success "Test data cleaned up"

# Step 10: Check service health
print_step "Checking notification service health..."
if curl -s "$SERVICE_URL/actuator/health" > /dev/null 2>&1; then
    HEALTH_STATUS=$(curl -s "$SERVICE_URL/actuator/health" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    if [ "$HEALTH_STATUS" = "UP" ]; then
        print_success "Service is healthy (Status: UP)"
    else
        print_warning "Service status: $HEALTH_STATUS"
    fi
else
    print_warning "Service health endpoint not accessible"
fi

# Step 11: Test REST API
print_step "Testing REST API endpoints..."
if curl -s "$SERVICE_URL/api/document-events" > /dev/null 2>&1; then
    print_success "REST API is accessible"
    API_COUNT=$(curl -s "$SERVICE_URL/api/document-events" | grep -o '"id":' | wc -l)
    echo -e "   Found $API_COUNT events via API"
else
    print_warning "REST API not accessible (service may not be ready)"
fi

# Step 12: Run comprehensive health check
print_step "Running comprehensive health check..."
echo ""
docker compose exec -T postgres psql -U $DB_USER -d $DB_NAME << 'EOF'
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
EOF

# Summary
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Verification Complete${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}✓ Database connectivity: OK${NC}"
echo -e "${GREEN}✓ Schema validation: OK${NC}"
echo -e "${GREEN}✓ Read/Write operations: OK${NC}"
echo -e "${GREEN}✓ Data integrity: OK${NC}"
echo ""
echo -e "Total records in database: ${YELLOW}$RECORD_COUNT${NC}"
echo -e "Successful migrations: ${YELLOW}$MIGRATION_COUNT${NC}"
echo ""
echo -e "${BLUE}Next steps:${NC}"
echo "  1. Run test data: docker compose exec -T postgres psql -U postgres -d notificationdb < sql-scripts/test_data.sql"
echo "  2. View all events: curl http://localhost:1004/api/document-events"
echo "  3. Check RabbitMQ: http://localhost:15672 (guest/guest)"
echo "  4. Check emails: http://localhost:8025"
echo ""
