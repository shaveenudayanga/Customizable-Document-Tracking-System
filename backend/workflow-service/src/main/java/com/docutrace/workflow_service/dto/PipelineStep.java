package com.docutrace.workflow_service.dto;

import jakarta.validation.constraints.*;

// Pipeline step
public record PipelineStep(
    @NotNull @Positive Integer stepNo,
    @NotBlank String deptKey,
    String instructions,
    Boolean notifyFlag
) {}
