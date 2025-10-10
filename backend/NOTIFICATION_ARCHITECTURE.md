# Notification Service Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Document Tracking System                             │
│                      Notification Integration Architecture                    │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────┐
│   API Gateway    │
│   (Port 8080)    │
└────────┬─────────┘
         │
         │ Routes to services
         ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                         Microservices Layer                                 │
└────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  User Service   │    │Document Service │    │ Workflow Service│
│   (Port 8081)   │    │   (Port 8082)   │    │   (Port 8083)   │
└────────┬────────┘    └────────┬────────┘    └────────┬────────┘
         │                      │                       │
         │ Events               │ Events                │ Events
         │                      │                       │
         ▼                      ▼                       ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                           RabbitMQ Message Broker                           │
│                              (Port 5672)                                    │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                         Exchanges                                     │ │
│  │                                                                       │ │
│  │  • workflow-events-exchange                                          │ │
│  │  • document-events-exchange                                          │ │
│  │  • user-events-exchange                                              │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                         Queues                                        │ │
│  │                                                                       │ │
│  │  Workflow Queues:                                                    │ │
│  │    • workflow.started                                                │ │
│  │    • workflow.task.completed                                         │ │
│  │    • workflow.completed                                              │ │
│  │    • workflow.rejected                                               │ │
│  │                                                                       │ │
│  │  Document Queues:                                                    │ │
│  │    • document.created                                                │ │
│  │    • document.updated                                                │ │
│  │    • document.approved                                               │ │
│  │    • document.rejected                                               │ │
│  │    • document.error                                                  │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────┬───────────────────────────────────────────┘
                                 │
                                 │ Consumes Events
                                 ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                      Notification Service                                   │
│                         (Port 1004)                                         │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                    Event Listeners                                    │ │
│  │                                                                       │ │
│  │  • WorkflowEventHandler                                              │ │
│  │  • DocumentEventHandler                                              │ │
│  │  • (Future: UserEventHandler)                                        │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │                  NotificationService                                  │ │
│  │                                                                       │ │
│  │  Handles:                                                            │ │
│  │  • Email composition                                                 │ │
│  │  • Template management                                               │ │
│  │  • Delivery orchestration                                            │ │
│  │  • Event deduplication                                               │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐ │
│  │              DocumentEventRepository                                  │ │
│  │              (Event audit trail)                                      │ │
│  └──────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────┬───────────────────────────────────────────┘
                                 │
                                 │ Sends notifications
                                 ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                      Delivery Channels                                      │
└────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Email (SMTP)   │    │  SMS (Future)   │    │ Web Push (Future)│
│   ✅ Active     │    │  📝 Planned     │    │  📝 Planned      │
└─────────────────┘    └─────────────────┘    └─────────────────┘

┌────────────────────────────────────────────────────────────────────────────┐
│                           Data Storage                                      │
└────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   user_db       │    │  document_db    │    │  workflow_db    │
│  (PostgreSQL)   │    │  (PostgreSQL)   │    │  (PostgreSQL)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘

                       ┌─────────────────┐
                       │notification_db  │
                       │  (PostgreSQL)   │
                       │                 │
                       │ Stores:         │
                       │ • Event history │
                       │ • Audit trail   │
                       └─────────────────┘
```

## Event Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    Workflow Event Flow Example                              │
└─────────────────────────────────────────────────────────────────────────────┘

1. User Action
   ↓
┌──────────────────────┐
│  Workflow Service    │
│  (Business Logic)    │
└──────────┬───────────┘
           │
           │ Creates event
           ▼
┌──────────────────────┐
│WorkflowEventPublisher│  Event: {
│  publishWorkflow     │    eventId: "uuid-123",
│  Started()           │    documentId: 456,
└──────────┬───────────┘    initiator: "john",
           │                ownerEmail: "john@example.com"
           │              }
           │
           │ Sends to RabbitMQ
           ▼
┌──────────────────────────────────────┐
│         RabbitMQ Exchange            │
│    (workflow-events-exchange)        │
└──────────┬───────────────────────────┘
           │
           │ Routes by key: "workflow.started"
           ▼
┌──────────────────────────────────────┐
│      RabbitMQ Queue                  │
│    (workflow.started)                │
└──────────┬───────────────────────────┘
           │
           │ Consumes event
           ▼
┌──────────────────────────────────────┐
│    Notification Service              │
│    WorkflowEventHandler              │
│    @RabbitListener                   │
└──────────┬───────────────────────────┘
           │
           │ Processes event
           ▼
┌──────────────────────────────────────┐
│    NotificationService               │
│    sendWorkflowStartedNotification() │
└──────────┬───────────────────────────┘
           │
           ├─────────────────────────────┐
           │                             │
           ▼                             ▼
┌──────────────────────┐    ┌──────────────────────┐
│  Email Delivery      │    │  Event Storage       │
│  (SMTP Server)       │    │  (Audit Trail)       │
└──────────────────────┘    └──────────────────────┘
           │                             │
           ▼                             ▼
┌──────────────────────┐    ┌──────────────────────┐
│  User Inbox          │    │  notification_db     │
│  ✉️ Notification     │    │  📊 Event Record     │
└──────────────────────┘    └──────────────────────┘
```

## Service Communication Patterns

### Before Integration (REST-based)
```
┌─────────────────┐                           ┌─────────────────┐
│Workflow Service │───── HTTP POST ─────────>│  User Service   │
│                 │  /api/notifications       │                 │
│                 │  {username, message}      │  Stores in DB   │
└─────────────────┘                           └─────────────────┘
                                                      │
                                                      │ Direct DB
                                                      ▼
                                              ┌─────────────────┐
                                              │user_notification│
                                              │     table       │
                                              └─────────────────┘

❌ Problems:
  • Tight coupling between services
  • User service must handle notification logic
  • Notification table in wrong database
  • Synchronous blocking calls
  • No retry mechanism
```

### After Integration (Event-driven)
```
┌─────────────────┐                           ┌─────────────────┐
│Workflow Service │───── Publish Event ─────>│    RabbitMQ     │
│                 │   (async, non-blocking)   │   (Message Bus) │
└─────────────────┘                           └────────┬────────┘
                                                       │
                                                       │ Delivers
                                                       ▼
                                              ┌─────────────────┐
                                              │ Notification    │
                                              │    Service      │
                                              └────────┬────────┘
                                                       │
                                                       ▼
                                              ┌─────────────────┐
                                              │  Email/SMS/Web  │
                                              │   Delivery      │
                                              └─────────────────┘

✅ Benefits:
  • Loose coupling via events
  • Dedicated notification service
  • Proper separation of concerns
  • Asynchronous processing
  • Built-in retry and resilience
```

## Data Flow

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        Event Processing Flow                                │
└────────────────────────────────────────────────────────────────────────────┘

1. EVENT CREATION
   Service creates event object with:
   • Unique eventId (UUID)
   • Business data (documentId, taskId, etc.)
   • User information (email, username)
   • Timestamp

2. SERIALIZATION
   Event converted to JSON by Jackson:
   {
     "eventId": "550e8400-e29b-41d4-a716-446655440000",
     "documentId": 123,
     "initiator": "john.doe",
     "ownerEmail": "john@example.com",
     "occurredAt": "2025-10-09T10:30:00Z"
   }

3. PUBLISHING
   RabbitTemplate sends to exchange with routing key:
   • Exchange: workflow-events-exchange
   • Routing Key: workflow.started
   • Message: JSON event

4. ROUTING
   RabbitMQ routes message to bound queue:
   workflow.started queue receives message

5. CONSUMPTION
   @RabbitListener method invoked with deserialized event

6. DEDUPLICATION
   Check if eventId already processed (idempotency)

7. PROCESSING
   Generate and send notification

8. STORAGE
   Store event in database for audit trail

9. ACKNOWLEDGMENT
   Message acknowledged, removed from queue
```

## Failure Handling

```
┌────────────────────────────────────────────────────────────────────────────┐
│                      Error Handling Flow                                    │
└────────────────────────────────────────────────────────────────────────────┘

SCENARIO 1: RabbitMQ Connection Failure
┌─────────────────┐
│  Service tries  │
│  to publish     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Connection fail │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Log error       │
│ Continue        │  ✅ Service continues operating
└─────────────────┘  ❌ Notification not sent

SCENARIO 2: Invalid Event Data
┌─────────────────┐
│  Event received │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Validation fail │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Log warning     │
│ Reject message  │  ✅ Message removed from queue
└─────────────────┘  ❌ No notification sent

SCENARIO 3: SMTP Failure (Transient)
┌─────────────────┐
│ Email send fail │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Log error       │
│ Throw exception │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Message NACK'd  │
│ Returns to queue│  ✅ Will retry
└─────────────────┘  ⚠️ Configure max retries

SCENARIO 4: Duplicate Event
┌─────────────────┐
│ Event received  │
│ Same eventId    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Check DB exists │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Already exists  │
│ Skip processing │  ✅ Idempotent
└─────────────────┘  ✅ Acknowledge anyway
```

## Scaling Considerations

```
┌────────────────────────────────────────────────────────────────────────────┐
│                      Horizontal Scaling                                     │
└────────────────────────────────────────────────────────────────────────────┘

                     Load Balancer
                          │
         ┌────────────────┼────────────────┐
         ▼                ▼                ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│ Notification    │ │ Notification    │ │ Notification    │
│ Service         │ │ Service         │ │ Service         │
│ Instance 1      │ │ Instance 2      │ │ Instance 3      │
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │
         └───────────────────┼───────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │    RabbitMQ     │
                    │  (Clustered)    │
                    └─────────────────┘

• Each instance consumes from same queues
• RabbitMQ distributes messages (round-robin)
• Idempotency prevents duplicate notifications
• Scale by adding more instances
```

---

**Last Updated**: October 9, 2025  
**Version**: 1.0
