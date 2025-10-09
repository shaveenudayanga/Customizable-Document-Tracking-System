# Notification Service Integration Guide

## Overview

The `notification-service_new` is the centralized notification service that handles all notification delivery (email, SMS, web alerts, reminders) for the Document Tracking System. All other services communicate with this service via **RabbitMQ messaging** to trigger notifications.

## Architecture

### Event-Driven Integration Pattern

All services publish events to RabbitMQ exchanges, and the notification-service listens to these events and sends appropriate notifications.

```
┌─────────────────┐      ┌─────────────────┐      ┌──────────────────────┐
│  User Service   │─────>│   RabbitMQ      │─────>│ Notification Service │
└─────────────────┘      │   (Exchange)    │      └──────────────────────┘
                          │                 │              │
┌─────────────────┐      │   Queues:       │              ▼
│Document Service │─────>│   - workflow.*  │      ┌──────────────────────┐
└─────────────────┘      │   - document.*  │      │  Email/SMS/Web Push  │
                          │   - user.*      │      └──────────────────────┘
┌─────────────────┐      │                 │
│Workflow Service │─────>│                 │
└─────────────────┘      └─────────────────┘
```

## Notification Service Configuration

### RabbitMQ Configuration
Located in: `backend/notification-service_new/src/main/resources/application.properties`

```properties
# RabbitMQ Connection
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:guest}

# Workflow Events
notification.workflow-events-exchange=workflow-events-exchange
notification.workflow-started-queue=workflow.started
notification.task-completed-queue=workflow.task.completed
notification.workflow-completed-queue=workflow.completed
notification.workflow-rejected-queue=workflow.rejected

# Document Events
notification.document-events-exchange=document-events-exchange
notification.document-created-queue=document.created
notification.document-updated-queue=document.updated
notification.document-approved-queue=document.approved
notification.document-rejected-queue=document.rejected
notification.document-error-queue=document.error
```

### Event Listeners

The notification service automatically listens to these queues:
- **Workflow Events**: `WorkflowEventHandler.java`
- **Document Events**: `DocumentEventHandler.java`

## Integration: Workflow Service

### 1. Dependencies

Added to `pom.xml`:
```xml
<!-- RabbitMQ for event publishing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 2. RabbitMQ Configuration

Added to `application.yml`:
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

workflow:
  events:
    exchange: workflow-events-exchange
```

### 3. Event Models

Created event record classes in `com.docutrace.workflow_service.events`:

**WorkflowStartedEvent.java**
```java
public record WorkflowStartedEvent(
    String eventId,
    Long documentId,
    Long pipelineInstanceId,
    String processInstanceId,
    String initiator,
    Instant occurredAt,
    String ownerEmail
) {}
```

**TaskCompletedEvent.java**
```java
public record TaskCompletedEvent(
    String eventId,
    Long documentId,
    String taskId,
    String taskName,
    String completedBy,
    Boolean approved,
    String notes,
    Instant occurredAt,
    String ownerEmail
) {}
```

**WorkflowCompletedEvent.java**
```java
public record WorkflowCompletedEvent(
    String eventId,
    Long documentId,
    Long pipelineInstanceId,
    String processInstanceId,
    String completedBy,
    Instant occurredAt,
    String ownerEmail
) {}
```

**WorkflowRejectedEvent.java**
```java
public record WorkflowRejectedEvent(
    String eventId,
    Long documentId,
    Long pipelineInstanceId,
    String processInstanceId,
    String rejectedBy,
    Instant occurredAt,
    String ownerEmail
) {}
```

### 4. Event Publisher

Created `WorkflowEventPublisher.java`:
```java
@Component
public class WorkflowEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    
    public void publishWorkflowStarted(Long documentId, Long pipelineInstanceId, 
                                       String processInstanceId, String initiator, 
                                       String ownerEmail) {
        WorkflowStartedEvent event = new WorkflowStartedEvent(
            UUID.randomUUID().toString(),
            documentId,
            pipelineInstanceId,
            processInstanceId,
            initiator,
            Instant.now(),
            ownerEmail
        );
        
        rabbitTemplate.convertAndSend(
            "workflow-events-exchange", 
            "workflow.started", 
            event
        );
    }
    
    // Similar methods for other events...
}
```

### 5. Usage in Services

Updated `WorkflowEventListener.java` to use the event publisher:
```java
@Component
public class WorkflowEventListener {
    private final WorkflowEventPublisher workflowEventPublisher;
    
    private void onWorkflowStarted(WorkflowEvent event, Map<String, Object> metadata) {
        String initiator = (String) event.payload().get("initiator");
        String ownerEmail = (String) event.payload().get("ownerEmail");
        
        // Publish event to notification service
        workflowEventPublisher.publishWorkflowStarted(
            event.documentId(), 
            event.pipelineInstanceId(), 
            event.processInstanceId(), 
            initiator, 
            ownerEmail
        );
    }
}
```

## Removed Code

### User Service
Completely removed all notification-related code:
- ❌ `NotificationController.java`
- ❌ `NotificationService.java`
- ❌ `NotificationRepository.java`
- ❌ `Notification.java` (entity)
- ❌ `NotificationRequest.java` (DTO)
- ❌ `NotificationResponse.java` (DTO)

### Workflow Service
- ❌ `NotificationClient.java` (REST client to user-service)
- ❌ Configuration property `notifications.enabled`
- ❌ Configuration property `user-service.base-url` (for notifications)

## Event Structure Examples

### Workflow Started Event
```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "documentId": 123,
  "pipelineInstanceId": 456,
  "processInstanceId": "process-789",
  "initiator": "john.doe",
  "occurredAt": "2025-10-09T10:30:00Z",
  "ownerEmail": "john.doe@example.com"
}
```

### Task Completed Event
```json
{
  "eventId": "660e8400-e29b-41d4-a716-446655440001",
  "documentId": 123,
  "taskId": "task-001",
  "taskName": "Review Document",
  "completedBy": "jane.smith",
  "approved": true,
  "notes": "Approved with minor comments",
  "occurredAt": "2025-10-09T11:00:00Z",
  "ownerEmail": "jane.smith@example.com"
}
```

## How to Add Notifications to Other Services

### Step 1: Add RabbitMQ Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### Step 2: Configure RabbitMQ
```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

### Step 3: Create Event Models
Create record classes for your events with necessary fields.

### Step 4: Create Event Publisher
```java
@Component
public class DocumentEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String documentEventsExchange = "document-events-exchange";
    
    public void publishDocumentCreated(String documentId, String title, 
                                       String creator, String ownerEmail) {
        DocumentCreatedEvent event = new DocumentCreatedEvent(
            UUID.randomUUID().toString(),
            documentId,
            title,
            creator,
            ownerEmail,
            LocalDateTime.now()
        );
        
        rabbitTemplate.convertAndSend(
            documentEventsExchange, 
            "document.created", 
            event
        );
    }
}
```

### Step 5: Publish Events
```java
@Service
public class DocumentService {
    private final DocumentEventPublisher eventPublisher;
    
    public Document createDocument(CreateDocumentRequest request) {
        Document document = // ... create document
        
        // Publish notification event
        eventPublisher.publishDocumentCreated(
            document.getId(),
            document.getTitle(),
            document.getCreator(),
            document.getOwnerEmail()
        );
        
        return document;
    }
}
```

## Notification Types Supported

The notification service currently handles:

1. **Workflow Notifications**
   - Workflow started
   - Task completed (approved/reviewed)
   - Workflow completed
   - Workflow rejected

2. **Document Notifications** (ready for integration)
   - Document created
   - Document updated
   - Document approved
   - Document rejected
   - Document error

3. **User Notifications** (ready for integration)
   - User registration welcome
   - Password reset
   - Profile updated

## Notification Delivery Methods

The notification service supports multiple delivery channels:
- ✅ **Email**: Configured via `spring.mail.*` properties
- 🔄 **SMS**: Ready for integration (add SMS provider)
- 🔄 **Web Push**: Ready for integration (add push notification service)
- 🔄 **In-App**: Ready for integration (add WebSocket support)

## Testing

### Start RabbitMQ
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### Verify Event Publishing
1. Access RabbitMQ Management UI: http://localhost:15672 (guest/guest)
2. Navigate to **Queues** tab
3. Verify queues are created and receiving messages

### Test Event Flow
Use the provided `TestEventController` in notification-service to manually trigger test events:
```bash
POST http://localhost:1004/api/test-events
Content-Type: application/json

{
  "eventType": "WORKFLOW_STARTED",
  "documentId": 123,
  "ownerEmail": "test@example.com"
}
```

## Troubleshooting

### Events Not Received
1. Verify RabbitMQ is running
2. Check exchange and queue bindings in RabbitMQ UI
3. Verify routing keys match between publisher and listener
4. Check application logs for connection errors

### Emails Not Sent
1. Verify SMTP configuration in `application.properties`
2. For development, use tools like MailHog or Mailpit
3. Check notification-service logs for email errors

## Benefits of This Integration

✅ **Separation of Concerns**: Notification logic isolated in one service  
✅ **Decoupled Architecture**: Services don't need direct dependencies  
✅ **Reliability**: RabbitMQ provides message persistence and retry  
✅ **Scalability**: Notification service can be scaled independently  
✅ **Flexibility**: Easy to add new notification channels  
✅ **Maintainability**: Centralized notification templates and logic  

## Future Enhancements

- [ ] Add WebSocket support for real-time in-app notifications
- [ ] Integrate SMS provider (Twilio, AWS SNS)
- [ ] Add notification preferences per user
- [ ] Implement notification templates system
- [ ] Add notification scheduling and reminders
- [ ] Implement notification history and audit trail
- [ ] Add notification retry and dead-letter queue handling

## API Documentation

Notification service exposes OpenAPI documentation at:
- Swagger UI: `http://localhost:1004/swagger-ui.html`
- OpenAPI JSON: `http://localhost:1004/openapi`

---

**Last Updated**: October 9, 2025  
**Version**: 1.0  
**Maintained By**: Development Team
