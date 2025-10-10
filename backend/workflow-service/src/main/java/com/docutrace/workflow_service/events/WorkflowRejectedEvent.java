package com.docutrace.workflow_service.events;

import java.time.Instant;

public record WorkflowRejectedEvent(
        String eventId,
        Long documentId,
        Long pipelineInstanceId,
        String processInstanceId,
        String rejectedBy,
        Instant occurredAt,
        String ownerEmail) {}
