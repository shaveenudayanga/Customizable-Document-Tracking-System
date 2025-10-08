package com.docutrace.workflow_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WorkflowStatusResponse(
        Long pipelineInstanceId,
        Long documentId,
        String processInstanceId,
        String processDefinitionKey,
        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        List<String> activeActivityIds,
        List<ActiveTask> activeTasks
) {
    public record ActiveTask(
            String taskId,
            String taskName,
            String initiator,
            String activityId,
            String assignee
    ) {}
}
