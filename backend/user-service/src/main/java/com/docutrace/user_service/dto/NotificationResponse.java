package com.docutrace.user_service.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationResponse(
        Long id,
        String type,
        String message,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        Map<String, Object> metadata
) {}
