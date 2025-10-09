# Database Setup and Verification Guide

This guide provides comprehensive instructions for setting up, connecting to, and verifying the PostgreSQL database for the Notification Service.

## Overview

The notification service uses PostgreSQL 16 to store document and workflow events. The database schema is managed using Flyway migrations, ensuring consistent schema across all environments.

### Database Details
- **Database Name**: `notificationdb`
- **Host**: `localhost` (when running locally) or `postgres` (in Docker)
- **Port**: `35432` (mapped from container port 5432)
- **User**: `postgres`
- **Password**: `ima123`

## Quick Start

### 1. Start the Database

```bash
cd Backend
docker compose up -d postgres
```

### 2. Verify Database Connection

```bash
# Using docker exec
docker compose exec postgres psql -U postgres -d notificationdb -c "SELECT version();"

# Or connect interactively
docker compose exec postgres psql -U postgres -d notificationdb
```

### 3. Start All Services

```bash
docker compose up -d
```

### 4. Check Logs

```bash
# Check if Flyway migrations ran successfully
docker compose logs notification-service | grep -i flyway

# View all service logs
docker compose logs -f notification-service
```

## Database Schema

### Main Table: `document_events`

Stores all document and workflow events for auditing and idempotency.

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key (auto-increment) |
| `event_id` | VARCHAR(255) | Unique event identifier (for idempotency) |
| `event_type` | VARCHAR(100) | Event type (e.g., document.created, workflow.started) |
| `document_id` | VARCHAR(255) | Reference to the document |
| `data` | TEXT | JSON payload of the event |
| `created_at` | TIMESTAMP | When the event was first recorded |
| `updated_at` | TIMESTAMP | When the event was last updated |

### Indexes

- `idx_document_events_event_id` - For fast event_id lookups
- `idx_document_events_event_type` - For filtering by event type
- `idx_document_events_document_id` - For finding all events for a document
- `idx_document_events_created_at` - For time-based queries

## Event Types

The system handles the following event types:

### Document Events
- `document.created` - New document created
- `document.updated` - Document modified
- `document.approved` - Document approved
- `document.rejected` - Document rejected
- `document.error` - Error processing document

### Workflow Events
- `workflow.started` - Workflow initiated
- `workflow.task.completed` - Task in workflow completed
- `workflow.completed` - Workflow finished successfully
- `workflow.rejected` - Workflow rejected

## SQL Scripts

The `sql-scripts/` directory contains helpful SQL files for testing and verification:

### 1. `check_connectivity.sql`
Run this first to verify database connection and basic functionality.

```bash
docker compose exec -T postgres psql -U postgres -d notificationdb < sql-scripts/check_connectivity.sql
```

**What it checks:**
- Database connection and version
- User permissions
- Table and sequence existence
- Flyway migration status
- Basic INSERT/SELECT operations

### 2. `test_data.sql`
Insert sample data to test all event types.

```bash
docker compose exec -T postgres psql -U postgres -d notificationdb < sql-scripts/test_data.sql
```

**What it does:**
- Inserts 9 test events (all event types)
- Verifies test data insertion
- Provides cleanup commands

### 3. `verify_database.sql`
Comprehensive verification queries for production use.

```bash
docker compose exec -T postgres psql -U postgres -d notificationdb < sql-scripts/verify_database.sql
```

**What it provides:**
- Schema verification
- Data counts by event type
- Data quality checks (duplicates, NULLs)
- Performance metrics
- Sample queries for analysis

## Testing Database Connectivity

### Method 1: Using SQL Scripts (Recommended)

```bash
# Step 1: Check connectivity
docker compose exec -T postgres psql -U postgres -d notificationdb -f /dev/stdin < sql-scripts/check_connectivity.sql

# Step 2: Insert test data
docker compose exec -T postgres psql -U postgres -d notificationdb -f /dev/stdin < sql-scripts/test_data.sql

# Step 3: Verify everything
docker compose exec -T postgres psql -U postgres -d notificationdb -f /dev/stdin < sql-scripts/verify_database.sql
```

### Method 2: Using REST API

```bash
# Insert event via API
curl -X POST http://localhost:1004/api/document-events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "manual-test-001",
    "eventType": "test.manual",
    "documentId": "DOC-API-001",
    "data": {"note": "Testing via API", "user": "tester"}
  }'

# Retrieve all events
curl http://localhost:1004/api/document-events
```

### Method 3: Using RabbitMQ Test Endpoints

```bash
# Document created event
curl -X POST http://localhost:1004/api/test/document-created \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "DOC-3001",
    "title": "Test Document",
    "creator": "alice",
    "ownerEmail": "owner@example.com"
  }'

# Check if event was saved
docker compose exec postgres psql -U postgres -d notificationdb \
  -c "SELECT * FROM document_events WHERE document_id = 'DOC-3001';"
```

## Common Database Operations

### View Recent Events

```sql
SELECT 
    id,
    event_id,
    event_type,
    document_id,
    LEFT(data, 100) AS data_snippet,
    created_at
FROM document_events
ORDER BY created_at DESC
LIMIT 20;
```

### Count Events by Type

```sql
SELECT 
    event_type, 
    COUNT(*) AS count
FROM document_events
GROUP BY event_type
ORDER BY count DESC;
```

### Find Events for a Specific Document

```sql
SELECT * FROM document_events 
WHERE document_id = 'DOC-123' 
ORDER BY created_at;
```

### Check for Duplicate Event IDs

```sql
SELECT 
    event_id, 
    COUNT(*) AS duplicate_count
FROM document_events
GROUP BY event_id
HAVING COUNT(*) > 1;
```

### View Flyway Migration History

```sql
SELECT 
    installed_rank,
    version,
    description,
    script,
    installed_on,
    execution_time,
    success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Using Database GUI Tools

You can connect to the database using any PostgreSQL client:

### DBeaver
1. Create new connection
2. **Host**: `localhost`
3. **Port**: `35432`
4. **Database**: `notificationdb`
5. **User**: `postgres`
6. **Password**: `ima123`

### pgAdmin
1. Add new server
2. **Connection > Host**: `localhost`
3. **Connection > Port**: `35432`
4. **Connection > Maintenance database**: `notificationdb`
5. **Connection > Username**: `postgres`
6. **Connection > Password**: `ima123`

### psql Command Line
```bash
psql -h localhost -p 35432 -U postgres -d notificationdb
# Password: ima123
```

## Troubleshooting

### Database Not Starting

```bash
# Check logs
docker compose logs postgres

# Restart database
docker compose restart postgres
```

### Connection Refused

```bash
# Verify port is exposed
docker compose ps

# Check if database is healthy
docker compose exec postgres pg_isready -U postgres -d notificationdb
```

### Flyway Migration Failures

```bash
# Check migration status
docker compose exec postgres psql -U postgres -d notificationdb \
  -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;"

# View application logs
docker compose logs notification-service | grep -i flyway
```

### Data Not Being Saved

1. Check if RabbitMQ is running:
   ```bash
   docker compose ps rabbitmq
   ```

2. Verify service is connected to database:
   ```bash
   docker compose logs notification-service | grep -i "datasource"
   ```

3. Check for errors:
   ```bash
   docker compose logs notification-service | grep -i error
   ```

### Reset Database (Development Only)

```bash
# Stop services
docker compose down

# Remove volume (destroys all data)
docker volume rm backend_postgres_data

# Restart
docker compose up -d
```

## Backup and Restore

### Create Backup

```bash
# Full database dump
docker compose exec postgres pg_dump -U postgres notificationdb > backup_$(date +%Y%m%d_%H%M%S).sql

# Data only (no schema)
docker compose exec postgres pg_dump -U postgres --data-only notificationdb > data_backup.sql
```

### Restore from Backup

```bash
# Stop application (to prevent conflicts)
docker compose stop notification-service

# Restore
docker compose exec -T postgres psql -U postgres -d notificationdb < backup.sql

# Restart application
docker compose start notification-service
```

## Performance Tuning

### Monitor Query Performance

```sql
-- Enable query statistics
-- (already enabled in PostgreSQL 16 by default)

-- View slow queries
SELECT 
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    max_exec_time
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 10;
```

### Check Index Usage

```sql
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan AS times_used,
    idx_tup_read AS tuples_read
FROM pg_stat_user_indexes
WHERE tablename = 'document_events'
ORDER BY idx_scan DESC;
```

### Analyze Table

```bash
docker compose exec postgres psql -U postgres -d notificationdb \
  -c "ANALYZE document_events;"
```

## Production Recommendations

1. **Use Strong Passwords**: Change default password in production
2. **Enable SSL**: Configure SSL connections for security
3. **Regular Backups**: Set up automated backups
4. **Monitor Disk Space**: Ensure adequate storage for event logs
5. **Archive Old Data**: Consider archiving events older than X months
6. **Connection Pooling**: Use HikariCP (already configured in Spring Boot)
7. **Monitor Performance**: Set up alerts for slow queries

## Additional Resources

- [PostgreSQL 16 Documentation](https://www.postgresql.org/docs/16/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

## Support

For issues or questions:
1. Check application logs: `docker compose logs notification-service`
2. Check database logs: `docker compose logs postgres`
3. Verify SQL scripts in `sql-scripts/` directory
4. Review Flyway migration files in `src/main/resources/db/migration/`
