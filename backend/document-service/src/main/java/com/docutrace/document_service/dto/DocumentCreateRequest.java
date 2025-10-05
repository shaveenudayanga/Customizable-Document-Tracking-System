package com.docutrace.document_service.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new document.
 */
public record DocumentCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 100) String documentType,
        @Size(max = 10_000) String description,
        @NotNull UUID ownerUserId,
        @NotBlank @Size(max = 50) String status,
        @Size(max = 512) String qrPath,
        @Size(max = 512) String fileDir
) {
}
