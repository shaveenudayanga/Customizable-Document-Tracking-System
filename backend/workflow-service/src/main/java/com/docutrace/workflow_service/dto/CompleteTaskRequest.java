package com.docutrace.workflow_service.dto;

import jakarta.validation.constraints.*;

// Complete task request
public record CompleteTaskRequest(
    @NotBlank String userId,
    String notes,
    @NotNull Boolean approved
) {}
