package com.docutrace.document_service.events;

import java.time.LocalDateTime;

/**
 * Event published when a new document is created
 * This event will be consumed by notification-service to send notifications
 */
public record DocumentCreatedEvent(
        String eventId,
        String documentId,
        String title,
        String creator,
        String ownerEmail,
        LocalDateTime createdAt) {}
