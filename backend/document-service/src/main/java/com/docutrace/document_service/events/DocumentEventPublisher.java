package com.docutrace.document_service.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Publisher for document-related events that trigger notifications
 * 
 * Usage example in DocumentService:
 * 
 * <pre>
 * {@code
 * @Service
 * public class DocumentService {
 *     private final DocumentEventPublisher eventPublisher;
 *     
 *     public Document createDocument(CreateDocumentRequest request) {
 *         Document document = // ... create and save document
 *         
 *         // Publish notification event
 *         eventPublisher.publishDocumentCreated(
 *             document.getId().toString(),
 *             document.getTitle(),
 *             request.getCreator(),
 *             document.getOwnerEmail()
 *         );
 *         
 *         return document;
 *     }
 * }
 * }
 * </pre>
 */
@Slf4j
@Component
public class DocumentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String documentEventsExchange;

    public DocumentEventPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${document.events.exchange:document-events-exchange}") String documentEventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.documentEventsExchange = documentEventsExchange;
    }

    /**
     * Publish document created event
     * 
     * @param documentId Unique identifier of the document
     * @param title Title of the document
     * @param creator Username of the document creator
     * @param ownerEmail Email address to send notification
     */
    public void publishDocumentCreated(String documentId, String title, String creator, String ownerEmail) {
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                UUID.randomUUID().toString(),
                documentId,
                title,
                creator,
                ownerEmail,
                LocalDateTime.now()
        );
        
        String routingKey = "document.created";
        publishEvent(event, routingKey);
    }

    /**
     * Publish document updated event
     * 
     * @param documentId Unique identifier of the document
     * @param title Title of the document
     * @param updater Username of the person who updated the document
     * @param ownerEmail Email address to send notification
     */
    public void publishDocumentUpdated(String documentId, String title, String updater, String ownerEmail) {
        DocumentUpdatedEvent event = new DocumentUpdatedEvent(
                UUID.randomUUID().toString(),
                documentId,
                title,
                updater,
                ownerEmail,
                LocalDateTime.now()
        );
        
        String routingKey = "document.updated";
        publishEvent(event, routingKey);
    }

    /**
     * Publish document approved event
     * 
     * @param documentId Unique identifier of the document
     * @param approver Username of the person who approved the document
     * @param ownerEmail Email address to send notification
     */
    public void publishDocumentApproved(String documentId, String approver, String ownerEmail) {
        DocumentApprovedEvent event = new DocumentApprovedEvent(
                UUID.randomUUID().toString(),
                documentId,
                approver,
                ownerEmail,
                LocalDateTime.now()
        );
        
        String routingKey = "document.approved";
        publishEvent(event, routingKey);
    }

    /**
     * Publish document rejected event
     * 
     * @param documentId Unique identifier of the document
     * @param rejector Username of the person who rejected the document
     * @param reason Reason for rejection
     * @param ownerEmail Email address to send notification
     */
    public void publishDocumentRejected(String documentId, String rejector, String reason, String ownerEmail) {
        DocumentRejectedEvent event = new DocumentRejectedEvent(
                UUID.randomUUID().toString(),
                documentId,
                rejector,
                reason,
                ownerEmail,
                LocalDateTime.now()
        );
        
        String routingKey = "document.rejected";
        publishEvent(event, routingKey);
    }

    private void publishEvent(Object event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(documentEventsExchange, routingKey, event);
            log.info("Published event to exchange '{}' with routing key '{}': {}", 
                    documentEventsExchange, routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish event to exchange '{}' with routing key '{}': {}", 
                    documentEventsExchange, routingKey, event, e);
        }
    }
}
