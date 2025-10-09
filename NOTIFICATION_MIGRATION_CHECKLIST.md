# Notification Service Migration Checklist

This checklist helps you verify the notification service integration is complete and working correctly.

## ✅ Completed Tasks

### Workflow Service
- [x] Added RabbitMQ dependency (`spring-boot-starter-amqp`)
- [x] Created event model classes (WorkflowStartedEvent, TaskCompletedEvent, etc.)
- [x] Created WorkflowEventPublisher component
- [x] Created RabbitMQConfig with JSON message converter
- [x] Updated application.yml with RabbitMQ configuration
- [x] Refactored WorkflowEventListener to use event publisher
- [x] Removed NotificationClient class
- [x] Removed notification REST client configuration

### User Service
- [x] Removed NotificationController
- [x] Removed NotificationService
- [x] Removed NotificationRepository
- [x] Removed Notification entity
- [x] Removed NotificationRequest DTO
- [x] Removed NotificationResponse DTO
- [x] Removed notification-related database table

### Documentation
- [x] Created NOTIFICATION_INTEGRATION_GUIDE.md
- [x] Created NOTIFICATION_INTEGRATION_SUMMARY.md
- [x] Created QUICK_START_NOTIFICATIONS.md
- [x] Updated main README.md with notification information

### Example Code
- [x] Created DocumentEventPublisher example
- [x] Created document event model classes
- [x] Documented integration patterns

## 📝 Recommended Next Steps

### For Full System Integration

#### Document Service
- [ ] Add RabbitMQ dependency to pom.xml
- [ ] Add RabbitMQ configuration to application.yml
- [ ] Add RabbitMQConfig class
- [ ] Implement DocumentEventPublisher (example provided)
- [ ] Publish events in DocumentService methods:
  - [ ] publishDocumentCreated() after document creation
  - [ ] publishDocumentUpdated() after document updates
  - [ ] publishDocumentApproved() when document is approved
  - [ ] publishDocumentRejected() when document is rejected

#### Tracking Service
- [ ] Determine if tracking events need notifications
- [ ] If yes, follow same pattern as workflow-service
- [ ] Create TrackingEventPublisher if needed
- [ ] Publish events for significant tracking activities

#### User Service (Optional)
- [ ] Add RabbitMQ dependency if user notifications needed
- [ ] Create UserEventPublisher for:
  - [ ] User registration welcome emails
  - [ ] Password reset notifications
  - [ ] Profile update confirmations

### Infrastructure & Deployment

#### Development Environment
- [ ] Verify RabbitMQ is running (localhost:5672)
- [ ] Verify notification-service_new is running (port 1004)
- [ ] Test event flow from workflow-service to notification-service
- [ ] Verify emails are being sent (check logs or mail server)

#### Testing
- [ ] Write integration tests for event publishing
- [ ] Test RabbitMQ connection failures and recovery
- [ ] Test notification delivery for all event types
- [ ] Verify event deduplication works correctly

#### Production Readiness
- [ ] Configure production RabbitMQ cluster
- [ ] Set up RabbitMQ monitoring and alerting
- [ ] Configure production SMTP server for emails
- [ ] Set up dead-letter queues for failed events
- [ ] Implement retry logic for failed notifications
- [ ] Configure event persistence and backup

### API Gateway (Optional)
- [ ] Add route to notification-service if REST access needed
- [ ] Configure authentication for notification queries
- [ ] Document notification service endpoints

### Monitoring & Observability
- [ ] Set up RabbitMQ metrics collection
- [ ] Monitor queue depths and processing rates
- [ ] Track notification delivery success/failure rates
- [ ] Set up alerts for:
  - [ ] Queue depth exceeding threshold
  - [ ] RabbitMQ connection failures
  - [ ] Notification delivery failures

## 🧪 Testing Checklist

### Unit Tests
- [ ] Test event publisher methods
- [ ] Test event serialization/deserialization
- [ ] Test RabbitMQ configuration

### Integration Tests
- [ ] Test end-to-end event flow (service → RabbitMQ → notification-service)
- [ ] Test event publishing with RabbitMQ down (should log error, not crash)
- [ ] Test notification service processing with invalid events
- [ ] Test email delivery (using test SMTP server)

### Manual Testing
- [ ] Create a workflow and verify email is sent
- [ ] Complete a task and verify email is sent
- [ ] Check RabbitMQ management UI for queue activity
- [ ] Verify event deduplication (same eventId sent twice)

## 🔧 Configuration Verification

### Service Configuration Files

#### workflow-service/application.yml
```yaml
✓ spring.rabbitmq.host configured
✓ spring.rabbitmq.port configured
✓ spring.rabbitmq.username configured
✓ spring.rabbitmq.password configured
✓ workflow.events.exchange configured
```

#### notification-service_new/application.properties
```properties
✓ spring.rabbitmq.* configured
✓ notification.workflow-events-exchange configured
✓ notification.workflow-started-queue configured
✓ notification.task-completed-queue configured
✓ notification.workflow-completed-queue configured
✓ notification.workflow-rejected-queue configured
✓ spring.mail.* configured
```

### RabbitMQ Setup
- [ ] Exchange created: workflow-events-exchange
- [ ] Queue created and bound: workflow.started
- [ ] Queue created and bound: workflow.task.completed
- [ ] Queue created and bound: workflow.completed
- [ ] Queue created and bound: workflow.rejected

## 🚀 Deployment Checklist

### Pre-Deployment
- [ ] Review all configuration files
- [ ] Verify RabbitMQ connection strings
- [ ] Test in staging environment
- [ ] Document rollback procedure

### Deployment Order
1. [ ] Deploy notification-service_new
2. [ ] Verify RabbitMQ queues are created
3. [ ] Deploy workflow-service (with RabbitMQ integration)
4. [ ] Verify events are being published and consumed
5. [ ] Deploy other services as they're integrated

### Post-Deployment
- [ ] Monitor logs for errors
- [ ] Verify RabbitMQ message rates
- [ ] Check email delivery
- [ ] Test critical workflows end-to-end

## 📊 Success Metrics

After integration, you should observe:
- [ ] Zero notification code in user-service
- [ ] Zero REST calls from workflow-service to user-service for notifications
- [ ] All workflow events trigger appropriate notifications
- [ ] RabbitMQ queues show steady message flow
- [ ] No message accumulation in queues (processed quickly)
- [ ] Notification service logs show event processing
- [ ] Users receive timely email notifications

## ⚠️ Known Limitations

- SMS and web push notifications require additional integration
- In-app notifications require WebSocket implementation
- Notification preferences per user not yet implemented
- No notification retry mechanism for transient failures

## 🐛 Troubleshooting

If issues arise, check:
1. [ ] RabbitMQ is running and accessible
2. [ ] Exchange and queue names match in all services
3. [ ] Routing keys are correct
4. [ ] Event models match between publisher and consumer
5. [ ] SMTP server is configured correctly
6. [ ] ownerEmail field is populated in events
7. [ ] Services can connect to RabbitMQ (firewall, network)

## 📞 Support Resources

- **Integration Guide**: `backend/NOTIFICATION_INTEGRATION_GUIDE.md`
- **Quick Start**: `QUICK_START_NOTIFICATIONS.md`
- **RabbitMQ UI**: http://localhost:15672 (guest/guest)
- **Notification API**: http://localhost:1004/swagger-ui.html

## ✨ Future Enhancements

Consider implementing:
- [ ] User notification preferences
- [ ] Notification templates management UI
- [ ] SMS provider integration (Twilio, AWS SNS)
- [ ] Web push notification support
- [ ] In-app notification center with WebSocket
- [ ] Notification scheduling and reminders
- [ ] Advanced filtering and routing
- [ ] Multi-language notification support

---

**Version**: 1.0  
**Last Updated**: October 9, 2025  
**Status**: Core Integration Complete ✅
