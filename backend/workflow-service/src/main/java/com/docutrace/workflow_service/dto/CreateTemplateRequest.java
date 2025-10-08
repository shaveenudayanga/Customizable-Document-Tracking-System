package com.docutrace.workflow_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

// Create template request
public record CreateTemplateRequest(
    @NotBlank String name,
    String documentType,
    @NotEmpty List<@Valid PipelineStep> steps,
    Boolean isPermanent
) {}
