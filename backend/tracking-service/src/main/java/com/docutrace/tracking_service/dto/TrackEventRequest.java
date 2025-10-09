package com.docutrace.tracking_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

public record TrackEventRequest(
        @NotNull Long documentId,
        @NotBlank String eventType,
        String location,
        @NotBlank String scannedBy,
        String notes,
        String qrCode,
        BigDecimal latitude,
        BigDecimal longitude,
        Map<String, Object> metadata
) {}
