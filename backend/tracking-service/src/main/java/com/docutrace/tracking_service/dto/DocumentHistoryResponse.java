package com.docutrace.tracking_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentHistoryResponse(
        Long documentId,
        Integer totalEvents,
        String currentLocation,
        LocalDateTime lastUpdated,
        List<TrackEventResponse> events
) {}
