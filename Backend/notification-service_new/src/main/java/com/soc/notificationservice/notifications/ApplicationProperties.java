package com.soc.notificationservice.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification")
public record ApplicationProperties(
        String orderEventsExchange,
        String newOrdersQueue,
        String deliveredOrdersQueue,
        String cancelledOrdersQueue,
        String errorOrdersQueue,
        String supportEmail,
        // Document tracking properties
        String documentEventsExchange,
        String documentCreatedQueue,
        String documentUpdatedQueue,
        String documentApprovedQueue,
        String documentRejectedQueue,
        String documentErrorQueue,
        // Workflow events properties
        String workflowEventsExchange,
        String workflowStartedQueue,
        String taskCompletedQueue,
        String workflowCompletedQueue,
        String workflowRejectedQueue) {}
