package com.docutrace.document_service.integration;

import com.docutrace.document_service.config.IntegrationProperties;
import com.docutrace.document_service.integration.client.TrackingServiceClient;
import com.docutrace.document_service.integration.client.WorkflowServiceClient;
import com.docutrace.document_service.integration.event.DocumentLifecycleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentIntegrationCoordinator {

    private final WorkflowServiceClient workflowServiceClient;
    private final TrackingServiceClient trackingServiceClient;
    private final IntegrationProperties integrationProperties;

    @Async
    @EventListener
    public void onDocumentCreated(DocumentLifecycleEvent event) {
        Long documentId = event.document().id();
        String initiator = event.document().ownerUserId() != null
                ? event.document().ownerUserId().toString()
                : "system";
        Long templateId = integrationProperties.getWorkflow().getDefaultTemplateId();

        log.info("Received document lifecycle event for document {} - triggering integrations", documentId);
        workflowServiceClient.startWorkflow(documentId, initiator, templateId);
        trackingServiceClient.registerQrCode(documentId, event.qrCodeBase64(), initiator);
        trackingServiceClient.recordSubmission(documentId, initiator);
    }
}
