package com.docutrace.workflow_service.event;

import java.time.Instant;
import java.util.Map;

public record WorkflowEvent(
        WorkflowEventType type,
        Long documentId,
        Long pipelineInstanceId,
        String processInstanceId,
        Map<String, Object> payload,
        Instant occurredAt
) {
    public static WorkflowEvent of(WorkflowEventType type,
                                   Long documentId,
                                   Long pipelineInstanceId,
                                   String processInstanceId,
                                   Map<String, Object> payload) {
        return new WorkflowEvent(type, documentId, pipelineInstanceId, processInstanceId,
                payload == null ? Map.of() : Map.copyOf(payload), Instant.now());
    }
}
