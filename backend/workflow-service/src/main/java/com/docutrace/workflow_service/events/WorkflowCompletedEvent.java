package com.docutrace.workflow_service.events;

import java.time.Instant;

public record WorkflowCompletedEvent(
        String eventId,
        Long documentId,
        Long pipelineInstanceId,
        String processInstanceId,
        String completedBy,
        Instant occurredAt,
        String ownerEmail) {}
