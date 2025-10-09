package com.soc.notificationservice.notifications.domain.models;

import java.time.LocalDateTime;

public record DocumentErrorEvent(String eventId, String documentId, String reason, LocalDateTime occurredAt) {}
