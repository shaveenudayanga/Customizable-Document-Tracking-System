# Quick Reference Guide - Database Commands

## Quick Start Commands

### 1. Start Database
```bash
cd Backend
docker compose up -d postgres
```

### 2. Check Database Status
```bash
docker compose exec postgres pg_isready -U postgres -d notificationdb
```

### 3. Connect to Database
```bash
docker compose exec postgres psql -U postgres -d notificationdb
```

### 4. Initialize Schema (First Time Only)
```bash
cd notification-service_new
docker compose -f ../docker-compose.yml exec -T postgres psql -U postgres -d notificationdb < src/main/resources/db/migration/V1__init_schema.sql
```

### 5. Run Tests
```bash
# Check connectivity
docker compose -f ../docker-compose.yml exec -T postgres psql -U postgres -d notificationdb < sql-scripts/check_connectivity.sql

# Insert test data
docker compose -f ../docker-compose.yml exec -T postgres psql -U postgres -d notificationdb < sql-scripts/test_data.sql

# Verify everything
docker compose -f ../docker-compose.yml exec -T postgres psql -U postgres -d notificationdb < sql-scripts/verify_database.sql
```

## Common Queries

### View All Events
```sql
SELECT id, event_id, event_type, document_id, created_at 
FROM document_events 
ORDER BY created_at DESC;
```

### Count by Event Type
```sql
SELECT event_type, COUNT(*) AS count 
FROM document_events 
GROUP BY event_type 
ORDER BY count DESC;
```

### Find Events by Document ID
```sql
SELECT * FROM document_events 
WHERE document_id = 'DOC-TEST-001' 
ORDER BY created_at;
```

### Recent Events (Last 20)
```sql
SELECT id, event_id, event_type, document_id, 
       LEFT(data, 100) AS data_snippet, created_at
FROM document_events
ORDER BY created_at DESC
LIMIT 20;
```

## One-Liners

### Quick Health Check
```bash
docker compose exec postgres psql -U postgres -d notificationdb -c "SELECT COUNT(*) FROM document_events;"
```

### View Table Structure
```bash
docker compose exec postgres psql -U postgres -d notificationdb -c "\d document_events"
```

### View Indexes
```bash
docker compose exec postgres psql -U postgres -d notificationdb -c "SELECT indexname, indexdef FROM pg_indexes WHERE tablename = 'document_events';"
```

### Delete Test Data
```bash
docker compose exec postgres psql -U postgres -d notificationdb -c "DELETE FROM document_events WHERE event_id LIKE 'test-%';"
```

### Export Data
```bash
docker compose exec postgres pg_dump -U postgres notificationdb > backup.sql
```

### Import Data
```bash
docker compose exec -T postgres psql -U postgres -d notificationdb < backup.sql
```

## Troubleshooting

### Reset Database (Development Only)
```bash
docker compose down
docker volume rm backend_postgres_data
docker compose up -d postgres
```

### View Logs
```bash
docker compose logs -f postgres
```

### Check Connection Parameters
```bash
docker compose exec postgres env | grep POSTGRES
```

## Event Types Reference

### Document Events
- `document.created`
- `document.updated`
- `document.approved`
- `document.rejected`
- `document.error`

### Workflow Events
- `workflow.started`
- `workflow.task.completed`
- `workflow.completed`
- `workflow.rejected`

## Database Connection Details

- **Host**: localhost (or `postgres` in Docker network)
- **Port**: 35432
- **Database**: notificationdb
- **User**: postgres
- **Password**: ima123

## GUI Tools

### psql
```bash
psql -h localhost -p 35432 -U postgres -d notificationdb
```

### DBeaver/pgAdmin
- Host: localhost
- Port: 35432
- Database: notificationdb
- User: postgres
- Password: ima123
