package com.soc.notificationservice.notifications.domain.models;

import java.time.Instant;

public record WorkflowRejectedEvent(
        String eventId,
        Long documentId,
        Long pipelineInstanceId,
        String processInstanceId,
        String rejectedBy,
        Instant occurredAt,
        String ownerEmail) {}
