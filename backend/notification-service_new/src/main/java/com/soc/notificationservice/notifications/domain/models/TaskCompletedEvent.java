package com.soc.notificationservice.notifications.domain.models;

import java.time.Instant;

public record TaskCompletedEvent(
        String eventId,
        Long documentId,
        Long pipelineInstanceId,
        String processInstanceId,
        String taskId,
        String taskName,
        Boolean approved,
        String completedBy,
        String notes,
        Instant occurredAt,
        String ownerEmail) {}
