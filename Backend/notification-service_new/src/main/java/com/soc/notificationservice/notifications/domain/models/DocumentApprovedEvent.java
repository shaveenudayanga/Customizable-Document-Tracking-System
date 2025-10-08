package com.soc.notificationservice.notifications.domain.models;

import java.time.LocalDateTime;

public record DocumentApprovedEvent(
        String eventId, String documentId, String approver, String ownerEmail, LocalDateTime approvedAt) {}
