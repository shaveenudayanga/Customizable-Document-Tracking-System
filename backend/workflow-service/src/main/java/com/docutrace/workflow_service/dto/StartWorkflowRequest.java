package com.docutrace.workflow_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

// Start workflow request
public record StartWorkflowRequest(
    @NotNull Long documentId,
    Long templateId,
    List<@Valid PipelineStep> customSteps,
    String initiator
) {}
