package com.soc.notificationservice.notifications.domain.models;

import java.time.LocalDateTime;

public record DocumentUpdatedEvent(
        String eventId, String documentId, String title, String updater, String ownerEmail, LocalDateTime updatedAt) {}
