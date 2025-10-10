# Notification Service Fix Summary

## Issues Identified

1. **RabbitMQ Not Running**: The notification service requires RabbitMQ for message queuing, but it's not installed/running
2. **Port Configuration Mismatch**: Gateway was configured to route to port 1004, but service runs on 8085
3. **Not Included in Startup Script**: The notification service wasn't part of the automated startup

## Fixes Applied

### 1. Added to Startup Script
- Updated `start-all-backend.sh` to include `notification-service_new` in the SERVICES array
- Service will now start/stop/restart with other backend services

### 2. Updated Gateway Configuration
- Changed notification service URI from `http://localhost:1004` to `http://localhost:8085`
- File: `backend/api-gateway/src/main/resources/application.yml`

## Current Status

### ✅ Completed
- Service added to startup script
- Gateway port configuration fixed
- Service can start without RabbitMQ (will retry connections in background)

### ⚠️ Pending (Optional)
To fully enable notification features with RabbitMQ:

1. **Install RabbitMQ**:
   ```bash
   sudo apt update
   sudo apt install rabbitmq-server
   sudo systemctl start rabbitmq-server
   sudo systemctl enable rabbitmq-server
   ```

2. **Verify RabbitMQ**:
   ```bash
   sudo systemctl status rabbitmq-server
   sudo rabbitmqctl status
   ```

3. **RabbitMQ Management UI** (optional):
   ```bash
   sudo rabbitmq-plugins enable rabbitmq_management
   # Access at: http://localhost:15672 (guest/guest)
   ```

## Service Configuration

### Port: 8085
### Database: notification_db (PostgreSQL)
### Queues:
- Document events: `document.created`, `document.updated`, `document.approved`, `document.rejected`
- Workflow events: `workflow.started`, `workflow.completed`, `workflow.rejected`, `task.completed`

## Testing

### Start All Services:
```bash
cd backend
./start-all-backend.sh start
```

### Check Notification Service:
```bash
curl http://localhost:8085/actuator/health
curl http://localhost:8080/api/notifications/actuator/health  # via gateway
```

### View Logs:
```bash
tail -f backend/logs/notification-service_new.log
```

## Notes

- The service will continue attempting to connect to RabbitMQ but won't crash
- Notification features (email, events) will work once RabbitMQ is running
- The service has OpenAPI/Swagger docs at: `http://localhost:8085/swagger-ui/index.html`
