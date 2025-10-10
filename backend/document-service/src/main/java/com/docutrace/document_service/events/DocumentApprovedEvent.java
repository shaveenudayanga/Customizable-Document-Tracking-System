package com.docutrace.document_service.events;

import java.time.LocalDateTime;

/**
 * Event published when a document is approved
 * This event will be consumed by notification-service to send notifications
 */
public record DocumentApprovedEvent(
        String eventId,
        String documentId,
        String approver,
        String ownerEmail,
        LocalDateTime approvedAt) {}
