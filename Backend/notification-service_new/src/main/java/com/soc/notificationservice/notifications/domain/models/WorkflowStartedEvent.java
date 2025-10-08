package com.soc.notificationservice.notifications.domain.models;

import java.time.Instant;

public record WorkflowStartedEvent(
        String eventId,
        Long documentId,
        Long pipelineInstanceId,
        String processInstanceId,
        String initiator,
        Instant occurredAt,
        String ownerEmail) {}
