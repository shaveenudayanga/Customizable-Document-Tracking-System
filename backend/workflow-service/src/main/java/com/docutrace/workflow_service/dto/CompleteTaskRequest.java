package com.docutrace.workflow_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

// Complete task request
public record CompleteTaskRequest(
    @NotBlank String userId,
    String notes,
    @NotNull Boolean approved,
    @Size(min = 2, max = 2, message = "documentStatuses must contain department and approval statuses")
    List<@NotBlank String> documentStatuses,
    String location,
    BigDecimal latitude,
    BigDecimal longitude
) {}
