package com.docutrace.workflow_service.events;

import java.time.Instant;

public record WorkflowStartedEvent(
        String eventId,
        Long documentId,
        Long pipelineInstanceId,
        String processInstanceId,
        String initiator,
        Instant occurredAt,
        String ownerEmail) {}
