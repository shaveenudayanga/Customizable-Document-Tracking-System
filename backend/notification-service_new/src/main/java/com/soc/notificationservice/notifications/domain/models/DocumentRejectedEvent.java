package com.soc.notificationservice.notifications.domain.models;

import java.time.LocalDateTime;

public record DocumentRejectedEvent(
        String eventId,
        String documentId,
        String rejector,
        String ownerEmail,
        String reason,
        LocalDateTime rejectedAt) {}
