package com.docutrace.document_service.events;

import java.time.LocalDateTime;

/**
 * Event published when a document is rejected
 * This event will be consumed by notification-service to send notifications
 */
public record DocumentRejectedEvent(
        String eventId,
        String documentId,
        String rejector,
        String reason,
        String ownerEmail,
        LocalDateTime rejectedAt) {}
