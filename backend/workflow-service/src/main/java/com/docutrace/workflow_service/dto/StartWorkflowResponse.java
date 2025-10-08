package com.docutrace.workflow_service.dto;

// Start workflow response
public record StartWorkflowResponse(
    String processInstanceId,
    String processDefinitionKey,
    Long pipelineInstanceId
) {}
