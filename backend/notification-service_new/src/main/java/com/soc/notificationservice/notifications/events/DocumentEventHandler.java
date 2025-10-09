package com.soc.notificationservice.notifications.events;

import com.soc.notificationservice.notifications.domain.DocumentEventEntity;
import com.soc.notificationservice.notifications.domain.DocumentEventRepository;
import com.soc.notificationservice.notifications.domain.NotificationService;
import com.soc.notificationservice.notifications.domain.models.DocumentApprovedEvent;
import com.soc.notificationservice.notifications.domain.models.DocumentCreatedEvent;
import com.soc.notificationservice.notifications.domain.models.DocumentErrorEvent;
import com.soc.notificationservice.notifications.domain.models.DocumentRejectedEvent;
import com.soc.notificationservice.notifications.domain.models.DocumentUpdatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DocumentEventHandler {
    private static final Logger log = LoggerFactory.getLogger(DocumentEventHandler.class);

    private final NotificationService notificationService;
    private final DocumentEventRepository documentEventRepository;
    private final ObjectMapper objectMapper;

    public DocumentEventHandler(
            NotificationService notificationService, DocumentEventRepository documentEventRepository, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.documentEventRepository = documentEventRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${notification.document-created-queue}")
    public void handle(DocumentCreatedEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate DocumentCreatedEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received DocumentCreatedEvent for documentId:{}", event.documentId());
    notificationService.sendDocumentCreatedNotification(event);
    documentEventRepository.save(new DocumentEventEntity(event.eventId(),
        "document.created", event.documentId(), toJson(event)));
    }

    @RabbitListener(queues = "${notification.document-updated-queue}")
    public void handle(DocumentUpdatedEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate DocumentUpdatedEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received DocumentUpdatedEvent for documentId:{}", event.documentId());
    notificationService.sendDocumentUpdatedNotification(event);
    documentEventRepository.save(new DocumentEventEntity(event.eventId(),
        "document.updated", event.documentId(), toJson(event)));
    }

    @RabbitListener(queues = "${notification.document-approved-queue}")
    public void handle(DocumentApprovedEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate DocumentApprovedEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received DocumentApprovedEvent for documentId:{}", event.documentId());
    notificationService.sendDocumentApprovedNotification(event);
    documentEventRepository.save(new DocumentEventEntity(event.eventId(),
        "document.approved", event.documentId(), toJson(event)));
    }

    @RabbitListener(queues = "${notification.document-rejected-queue}")
    public void handle(DocumentRejectedEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate DocumentRejectedEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received DocumentRejectedEvent for documentId:{}", event.documentId());
    notificationService.sendDocumentRejectedNotification(event);
    documentEventRepository.save(new DocumentEventEntity(event.eventId(),
        "document.rejected", event.documentId(), toJson(event)));
    }

    @RabbitListener(queues = "${notification.document-error-queue}")
    public void handle(DocumentErrorEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate DocumentErrorEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received DocumentErrorEvent for documentId:{}", event.documentId());
        notificationService.sendDocumentErrorEventNotification(event);
        documentEventRepository.save(new DocumentEventEntity(event.eventId(),
                "document.error", event.documentId(), toJson(event)));
    }

    private String toJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event to JSON for persistence: {}", e.getMessage());
            return null;
        }
    }
}
