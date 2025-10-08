package com.docutrace.workflow_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateTemplateRequest(
        @NotBlank String name,
        String documentType,
        @NotEmpty List<@Valid PipelineStep> steps,
        Boolean isPermanent
) {}
