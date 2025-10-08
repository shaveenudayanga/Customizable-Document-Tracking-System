// Error DTO: standardized API error response payload
package com.docutrace.document_service.controller.error;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {

    public static ApiErrorResponse of(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                Collections.emptyMap()
        );
    }

    public static ApiErrorResponse of(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        Map<String, String> errors = fieldErrors == null ? Collections.emptyMap() : Map.copyOf(fieldErrors);
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                errors
        );
    }
}
