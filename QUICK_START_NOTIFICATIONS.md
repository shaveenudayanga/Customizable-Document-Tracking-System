# Quick Start: Adding Notifications to Your Service

This guide shows you how to quickly add notification support to any service in the Document Tracking System.

## Prerequisites

- RabbitMQ running on localhost:5672 (or configured elsewhere)
- notification-service_new running on port 1004
- Your service running and able to connect to RabbitMQ

## Step-by-Step Integration

### Step 1: Add RabbitMQ Dependency

Add to your service's `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### Step 2: Configure RabbitMQ

Add to your `application.yml`:

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

# Your service event configuration
your-service:
  events:
    exchange: your-service-events-exchange  # e.g., document-events-exchange
```

### Step 3: Create Event Record Classes

Create event models in `com.docutrace.your_service.events` package:

```java
package com.docutrace.your_service.events;

import java.time.LocalDateTime;

public record YourEvent(
    String eventId,
    String entityId,
    String actionPerformedBy,
    String ownerEmail,
    LocalDateTime occurredAt
) {}
```

**Important Fields:**
- `eventId`: Unique identifier for this event (use UUID)
- `ownerEmail`: Email address to send notification to
- `occurredAt`: Timestamp of the event

### Step 4: Create RabbitMQ Configuration

Create `RabbitMQConfig.java` in your config package:

```java
package com.docutrace.your_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                         MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
```

### Step 5: Create Event Publisher

Create event publisher in `com.docutrace.your_service.events`:

```java
package com.docutrace.your_service.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class YourServiceEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String eventsExchange;

    public YourServiceEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${your-service.events.exchange}") String eventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventsExchange = eventsExchange;
    }

    public void publishYourEvent(String entityId, String actionPerformedBy, String ownerEmail) {
        YourEvent event = new YourEvent(
            UUID.randomUUID().toString(),
            entityId,
            actionPerformedBy,
            ownerEmail,
            LocalDateTime.now()
        );
        
        String routingKey = "your.event.type"; // e.g., "document.created"
        
        try {
            rabbitTemplate.convertAndSend(eventsExchange, routingKey, event);
            log.info("Published event: {} with routing key: {}", event, routingKey);
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event, e);
        }
    }
}
```

### Step 6: Use in Your Service

Inject and use the event publisher in your service:

```java
package com.docutrace.your_service.service;

import com.docutrace.your_service.events.YourServiceEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YourBusinessService {

    private final YourServiceEventPublisher eventPublisher;

    public void performAction(ActionRequest request) {
        // Your business logic here...
        
        // Publish notification event
        eventPublisher.publishYourEvent(
            entity.getId(),
            request.getUsername(),
            entity.getOwnerEmail()
        );
    }
}
```

## Routing Keys Convention

Use the following routing key patterns:

| Event Type | Routing Key | Example |
|------------|-------------|---------|
| Entity Created | `entity.created` | `document.created` |
| Entity Updated | `entity.updated` | `document.updated` |
| Entity Approved | `entity.approved` | `document.approved` |
| Entity Rejected | `entity.rejected` | `document.rejected` |
| Workflow Started | `workflow.started` | `workflow.started` |
| Task Completed | `workflow.task.completed` | `workflow.task.completed` |

## Testing Your Integration

### 1. Start Required Services

```bash
# Start RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# Start notification-service_new
cd backend/notification-service_new
./mvnw spring-boot:run

# Start your service
cd backend/your-service
./mvnw spring-boot:run
```

### 2. Verify RabbitMQ Connection

Access RabbitMQ Management UI:
- URL: http://localhost:15672
- Username: guest
- Password: guest

Check:
- ✅ Your exchange is created
- ✅ Queues are bound to the exchange
- ✅ Connections show your service connected

### 3. Trigger an Event

Use your service API to trigger an action that publishes an event:

```bash
curl -X POST http://localhost:YOUR_PORT/api/your-endpoint \
  -H "Content-Type: application/json" \
  -d '{"field": "value"}'
```

### 4. Verify Event Flow

Check the following:

1. **Your Service Logs**: Should show "Published event..."
2. **RabbitMQ UI**: Queue should show message count increase then decrease
3. **Notification Service Logs**: Should show "Received event..."
4. **Email**: Check email was sent (or check logs if using test SMTP)

## Common Issues and Solutions

### Issue: Connection Refused

```
Caused by: java.net.ConnectException: Connection refused
```

**Solution**: Verify RabbitMQ is running:
```bash
docker ps | grep rabbitmq
```

### Issue: Queue Not Found

```
Channel shutdown: channel error; protocol method: #method<channel.close>
```

**Solution**: 
- Verify exchange name matches in both publisher and notification-service
- Check routing keys are correctly configured
- Restart notification-service to recreate queues

### Issue: Events Not Being Processed

**Solution**:
- Check notification-service logs for errors
- Verify event structure matches expected format
- Ensure `ownerEmail` field is populated

### Issue: Serialization Errors

```
Could not convert message
```

**Solution**: 
- Ensure RabbitMQConfig has JavaTimeModule registered
- Use compatible data types (avoid complex nested objects)
- Verify all event fields are serializable

## Notification Service Configuration

The notification service listens to these queues (configured in `application.properties`):

### Workflow Events
- `workflow.started`
- `workflow.task.completed`
- `workflow.completed`
- `workflow.rejected`

### Document Events
- `document.created`
- `document.updated`
- `document.approved`
- `document.rejected`
- `document.error`

## Next Steps

After basic integration:

1. **Add More Events**: Create additional event types for other actions
2. **Customize Templates**: Update notification-service email templates
3. **Add Retry Logic**: Implement dead-letter queues for failed events
4. **Monitor**: Set up monitoring for event processing
5. **Test Scenarios**: Write integration tests for event publishing

## Example: Complete Integration for Document Service

See `backend/document-service/src/main/java/com/docutrace/document_service/events/` for complete example including:
- `DocumentCreatedEvent.java`
- `DocumentUpdatedEvent.java`
- `DocumentApprovedEvent.java`
- `DocumentRejectedEvent.java`
- `DocumentEventPublisher.java`

## Need Help?

1. Check `NOTIFICATION_INTEGRATION_GUIDE.md` for detailed documentation
2. Review workflow-service implementation as reference
3. Check RabbitMQ management UI for message flow
4. Review notification-service logs for processing errors

---

**Remember**: Always test in development environment before deploying to production!
