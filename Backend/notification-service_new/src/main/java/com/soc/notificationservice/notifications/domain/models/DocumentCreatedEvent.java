package com.soc.notificationservice.notifications.domain.models;

import java.time.LocalDateTime;

public record DocumentCreatedEvent(
        String eventId, String documentId, String title, String creator, String ownerEmail, LocalDateTime createdAt) {}
