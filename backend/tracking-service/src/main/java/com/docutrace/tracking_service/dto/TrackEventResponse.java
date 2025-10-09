package com.docutrace.tracking_service.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record TrackEventResponse(
        Long id,
        Long documentId,
        String eventType,
        String location,
        String scannedBy,
        String notes,
        String qrCode,
        Map<String, Object> metadata,
        LocalDateTime createdAt
) {}
