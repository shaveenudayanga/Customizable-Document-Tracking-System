package com.docutrace.workflow_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TemplateResponse(
        Long id,
        String name,
        String documentType,
        Boolean permanent,
        Integer version,
        String createdBy,
        LocalDateTime createdAt,
        List<PipelineStep> steps
) {}
