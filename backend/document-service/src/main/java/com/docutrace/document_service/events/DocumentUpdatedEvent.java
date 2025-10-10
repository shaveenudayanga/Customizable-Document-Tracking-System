package com.docutrace.document_service.events;

import java.time.LocalDateTime;

/**
 * Event published when a document is updated
 * This event will be consumed by notification-service to send notifications
 */
public record DocumentUpdatedEvent(
        String eventId,
        String documentId,
        String title,
        String updater,
        String ownerEmail,
        LocalDateTime updatedAt) {}
