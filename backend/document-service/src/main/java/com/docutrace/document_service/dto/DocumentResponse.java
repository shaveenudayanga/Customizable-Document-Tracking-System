// DTO: response payload representing document details
package com.docutrace.document_service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response payload representing document details exposed externally.
 */
public record DocumentResponse(
        Long id,
        String title,
        String documentType,
        String description,
        UUID ownerUserId,
        String status,
        String qrPath,
        String fileDir,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
