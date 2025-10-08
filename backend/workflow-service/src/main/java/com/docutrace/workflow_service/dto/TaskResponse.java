package com.docutrace.workflow_service.dto;

// Task response
public record TaskResponse(
    String taskId,
    String taskName,
    String processInstanceId,
    Long documentId,
    String deptKey,
    String instructions
) {}
