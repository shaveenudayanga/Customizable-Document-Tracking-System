# Docker Compose Production Deployment Guide

## 📦 Services Overview

The production deployment includes the following services:

### Infrastructure Services
1. **PostgreSQL** (port 5432) - Database for all microservices
2. **RabbitMQ** (ports 5672, 15672) - Message broker for event-driven architecture
3. **MailHog** (ports 1025, 8025) - Development SMTP server for testing emails

### Application Services
4. **User Service** (port 8081) - Authentication & user management
5. **Document Service** (port 8082) - Document management
6. **Workflow Service** (port 8083) - Workflow orchestration & event publishing
7. **Tracking Service** (port 8084) - Document location tracking
8. **Notification Service** (port 1004) - Centralized notification handling
9. **Frontend** (port 80) - React application
10. **Nginx** (ports 80, 443) - Reverse proxy & load balancer

---

## 🚀 Quick Start

### Prerequisites
- Docker Engine 20.10+
- Docker Compose 2.0+
- 8GB RAM minimum
- 20GB free disk space

### Start All Services

```bash
# Build and start all services
docker-compose -f docker-compose.prod.yml up -d

# Check service status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

### Stop All Services

```bash
# Stop and remove containers
docker-compose -f docker-compose.prod.yml down

# Stop and remove containers + volumes (⚠️ deletes data)
docker-compose -f docker-compose.prod.yml down -v
```

---

## 🗄️ Database Setup

### Automatic Database Creation

The `init-db.sql` script automatically creates all required databases:
- `user_db` - User service database
- `document_db` - Document service database
- `workflow_db` - Workflow service database
- `tracking_db` - Tracking service database
- `notification_db` - Notification service database

### Manual Database Access

```bash
# Connect to PostgreSQL
docker exec -it docutrace-postgres psql -U docutrace -d docutrace

# List all databases
\l

# Connect to specific database
\c user_db

# List tables
\dt
```

---

## 🐰 RabbitMQ Setup

### Access RabbitMQ Management UI

1. Open browser: `http://localhost:15672`
2. Login credentials:
   - **Username**: `guest`
   - **Password**: `guest`

### Verify Queues

After services start, check that these queues are created:

**Workflow Events:**
- `workflow.started`
- `workflow.task.completed`
- `workflow.completed`
- `workflow.rejected`

**Document Events:**
- `document.created`
- `document.updated`
- `document.approved`
- `document.rejected`
- `document.error`

### RabbitMQ CLI Commands

```bash
# View queue list
docker exec docutrace-rabbitmq rabbitmqctl list_queues

# View exchanges
docker exec docutrace-rabbitmq rabbitmqctl list_exchanges

# View bindings
docker exec docutrace-rabbitmq rabbitmqctl list_bindings
```

---

## 📧 Email Configuration

### Development (MailHog)

MailHog is included for development/testing. All emails are captured.

**Access MailHog UI**: `http://localhost:8025`

**Configuration** (already set in docker-compose):
```yaml
MAIL_HOST: mailhog
MAIL_PORT: 1025
```

### Production (Real SMTP)

For production, update the notification service environment variables:

```yaml
notification-service:
  environment:
    MAIL_HOST: smtp.gmail.com
    MAIL_PORT: 587
    MAIL_USERNAME: your-email@gmail.com
    MAIL_PASSWORD: your-app-password
```

**Gmail Setup:**
1. Enable 2FA on your Gmail account
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Use app password in MAIL_PASSWORD

**Other SMTP Providers:**
- **SendGrid**: smtp.sendgrid.net:587
- **AWS SES**: email-smtp.region.amazonaws.com:587
- **Mailgun**: smtp.mailgun.org:587

---

## 🔍 Service Health Checks

### Check All Services

```bash
# View service status
docker-compose -f docker-compose.prod.yml ps

# Check specific service health
docker inspect --format='{{.State.Health.Status}}' docutrace-user-service
```

### Health Check Endpoints

| Service | Health Check URL |
|---------|------------------|
| User Service | http://localhost:8081/api/health |
| Document Service | http://localhost:8082/api/health |
| Workflow Service | http://localhost:8083/api/health |
| Tracking Service | http://localhost:8084/api/health |
| Notification Service | http://localhost:1004/actuator/health |

### Manual Health Checks

```bash
# User Service
curl http://localhost:8081/api/health

# Notification Service
curl http://localhost:1004/actuator/health

# RabbitMQ
curl http://localhost:15672/api/healthchecks/node
```

---

## 📊 Monitoring & Logs

### View Logs

```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f notification-service

# Last 100 lines
docker-compose -f docker-compose.prod.yml logs --tail=100 workflow-service

# With timestamps
docker-compose -f docker-compose.prod.yml logs -f -t notification-service
```

### Log Locations (inside containers)

```bash
# Access container shell
docker exec -it docutrace-notification-service sh

# View application logs
tail -f /app/logs/application.log
```

### Monitor Resources

```bash
# Container resource usage
docker stats

# Specific service
docker stats docutrace-notification-service
```

---

## 🔧 Service Configuration

### Environment Variables

Each service can be configured via environment variables:

#### Workflow Service
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/workflow_db
RABBITMQ_HOST: rabbitmq
RABBITMQ_PORT: 5672
RABBITMQ_USERNAME: guest
RABBITMQ_PASSWORD: guest
```

#### Notification Service
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/notification_db
RABBITMQ_HOST: rabbitmq
RABBITMQ_PORT: 5672
MAIL_HOST: mailhog
MAIL_PORT: 1025
```

### Override Configuration

Create `.env` file in root directory:

```env
# Database
POSTGRES_PASSWORD=your-secure-password

# RabbitMQ
RABBITMQ_DEFAULT_USER=admin
RABBITMQ_DEFAULT_PASS=secure-password

# Email (Production)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

Then reference in docker-compose:
```yaml
environment:
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
```

---

## 🔄 Service Startup Order

Services start in this order due to dependencies:

1. **PostgreSQL** → Database must be ready first
2. **RabbitMQ** → Message broker must be available
3. **MailHog** → (Optional) SMTP server for emails
4. **Application Services** → Start after infrastructure
   - User Service
   - Document Service
   - Workflow Service (depends on RabbitMQ)
   - Tracking Service
   - Notification Service (depends on RabbitMQ)
5. **Frontend** → Starts after backend services
6. **Nginx** → Reverse proxy starts last

---

## 🧪 Testing the Setup

### 1. Verify Database Connections

```bash
# Check PostgreSQL
docker exec -it docutrace-postgres psql -U docutrace -c "\l"

# Expected: List of 5 databases
```

### 2. Verify RabbitMQ

```bash
# Access management UI
open http://localhost:15672

# Check queues are created
# Should see workflow.* and document.* queues
```

### 3. Test Notification Flow

```bash
# 1. Create a workflow (via API or frontend)
# 2. Check RabbitMQ UI - message should appear in queue
# 3. Check notification-service logs - should show event received
# 4. Check MailHog UI - email should appear
open http://localhost:8025
```

### 4. Test API Endpoints

```bash
# User Service
curl http://localhost:8081/api/health

# Notification Service
curl http://localhost:1004/actuator/health

# RabbitMQ
curl -u guest:guest http://localhost:15672/api/overview
```

---

## 🐛 Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs service-name

# Rebuild specific service
docker-compose -f docker-compose.prod.yml build --no-cache service-name
docker-compose -f docker-compose.prod.yml up -d service-name
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs docutrace-postgres

# Test connection
docker exec -it docutrace-postgres psql -U docutrace -d user_db -c "SELECT 1;"
```

### RabbitMQ Connection Issues

```bash
# Check RabbitMQ is running
docker ps | grep rabbitmq

# Check RabbitMQ logs
docker logs docutrace-rabbitmq

# Verify connectivity
docker exec docutrace-workflow-service ping rabbitmq
```

### No Emails Being Sent

```bash
# Check MailHog is running
docker ps | grep mailhog

# Check notification-service logs
docker logs docutrace-notification-service | grep -i "email\|smtp"

# Verify MailHog UI is accessible
curl http://localhost:8025
```

### Events Not Being Published

```bash
# Check workflow-service logs
docker logs docutrace-workflow-service | grep -i "published\|rabbitmq"

# Check RabbitMQ queues
open http://localhost:15672/#/queues

# Check notification-service is consuming
docker logs docutrace-notification-service | grep -i "received\|event"
```

---

## 🔐 Security Considerations

### Production Deployment

For production, update these settings:

1. **Database Passwords**
   ```yaml
   POSTGRES_PASSWORD: <strong-password>
   ```

2. **RabbitMQ Credentials**
   ```yaml
   RABBITMQ_DEFAULT_USER: admin
   RABBITMQ_DEFAULT_PASS: <strong-password>
   ```

3. **JWT Secrets**
   ```yaml
   JWT_SECRET: <256-bit-secret>
   ```

4. **HTTPS/SSL**
   - Add SSL certificates to `./ssl/` directory
   - Update nginx configuration
   - Expose port 443

5. **Remove MailHog** (development only)
   - Comment out mailhog service
   - Configure real SMTP server

---

## 📈 Scaling

### Scale Specific Services

```bash
# Scale notification service to 3 instances
docker-compose -f docker-compose.prod.yml up -d --scale notification-service=3

# Scale workflow service to 2 instances
docker-compose -f docker-compose.prod.yml up -d --scale workflow-service=2
```

### Load Balancing

Nginx automatically load balances between scaled instances.

---

## 💾 Backup & Restore

### Database Backup

```bash
# Backup all databases
docker exec docutrace-postgres pg_dumpall -U docutrace > backup.sql

# Backup specific database
docker exec docutrace-postgres pg_dump -U docutrace notification_db > notification_backup.sql
```

### Database Restore

```bash
# Restore from backup
docker exec -i docutrace-postgres psql -U docutrace < backup.sql
```

### RabbitMQ Backup

```bash
# Export definitions (exchanges, queues, bindings)
curl -u guest:guest http://localhost:15672/api/definitions > rabbitmq-definitions.json
```

---

## 📝 Service URLs Summary

| Service | URL | Purpose |
|---------|-----|---------|
| Frontend | http://localhost | Web application |
| User Service | http://localhost:8081 | User APIs |
| Document Service | http://localhost:8082 | Document APIs |
| Workflow Service | http://localhost:8083 | Workflow APIs |
| Tracking Service | http://localhost:8084 | Tracking APIs |
| Notification Service | http://localhost:1004 | Notification APIs |
| RabbitMQ UI | http://localhost:15672 | Message queue management |
| MailHog UI | http://localhost:8025 | Email testing |
| PostgreSQL | localhost:5432 | Database access |

---

## 🎯 Next Steps

After successful deployment:

1. ✅ **Verify all services are running**
   ```bash
   docker-compose -f docker-compose.prod.yml ps
   ```

2. ✅ **Check RabbitMQ queues created**
   - Visit http://localhost:15672

3. ✅ **Test notification flow**
   - Create a workflow
   - Check MailHog for email

4. ✅ **Monitor logs for errors**
   ```bash
   docker-compose -f docker-compose.prod.yml logs -f
   ```

5. ✅ **Set up production SMTP** (if deploying to production)

6. ✅ **Configure SSL/HTTPS** (if deploying to production)

---

**Version**: 1.0  
**Last Updated**: October 10, 2025  
**Status**: Production Ready ✅
