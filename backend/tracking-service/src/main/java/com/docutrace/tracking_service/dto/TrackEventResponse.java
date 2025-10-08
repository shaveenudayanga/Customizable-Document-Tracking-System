package com.docutrace.tracking_service.dto;

import java.time.LocalDateTime;

public record TrackEventResponse(
        Long id,
        Long documentId,
        String eventType,
        String location,
        String scannedBy,
        String notes,
        LocalDateTime createdAt
) {}
