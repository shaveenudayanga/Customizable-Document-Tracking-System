package com.docutrace.document_service.integration.client;

import com.docutrace.document_service.config.IntegrationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowServiceClient {

    private final RestTemplate restTemplate;
    private final IntegrationProperties integrationProperties;

    public void startWorkflow(Long documentId, String initiator, Long templateId) {
        if (!integrationProperties.getWorkflow().isAutoStartEnabled()) {
            log.debug("Workflow auto-start disabled - skipping start request for document {}", documentId);
            return;
        }

        String url = integrationProperties.getWorkflow().getBaseUrl() + "/api/workflow/start";
        StartWorkflowPayload payload = new StartWorkflowPayload(documentId, templateId, initiator);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Void.class);
            log.info("Triggered workflow start for document {} using template {}", documentId, templateId);
        } catch (Exception ex) {
            log.error("Failed to trigger workflow start for document {}", documentId, ex);
        }
    }

    private record StartWorkflowPayload(Long documentId, Long templateId, String initiator) {
    }
}
