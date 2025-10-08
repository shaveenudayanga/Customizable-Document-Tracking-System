// DTO: request payload for updating document status values sourced from the workflow service
package com.docutrace.document_service.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.springframework.lang.Nullable;

/**
 * Request payload representing a status update coming from the workflow service.
 * Exposes the department-level status and the approval status as a fixed-size list.
 */
public record DocumentStatusUpdateRequest(
        @NotEmpty(message = "statuses must include department and approval values")
        @Size(min = 2, max = 2, message = "statuses must contain exactly two elements")
        List<@NotBlank(message = "status values cannot be blank") String> statuses,
        @Nullable @Size(max = 128, message = "processInstanceId must be at most 128 characters") String processInstanceId
) {
}
