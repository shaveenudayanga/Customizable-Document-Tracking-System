package com.docutrace.workflow_service.events;

import java.time.Instant;

public record TaskCompletedEvent(
        String eventId,
        Long documentId,
        String taskId,
        String taskName,
        String completedBy,
        Boolean approved,
        String notes,
        Instant occurredAt,
        String ownerEmail) {}
