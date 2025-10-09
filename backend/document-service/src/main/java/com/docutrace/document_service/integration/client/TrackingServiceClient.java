package com.docutrace.document_service.integration.client;

import com.docutrace.document_service.config.IntegrationProperties;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackingServiceClient {

    private final RestTemplate restTemplate;
    private final IntegrationProperties integrationProperties;

    public void registerQrCode(Long documentId, String qrCodeBase64, String registeredBy) {
        if (!integrationProperties.getTracking().isRegisterQr()) {
            log.debug("QR registration disabled - skipping registration for document {}", documentId);
            return;
        }
        if (qrCodeBase64 == null) {
            log.warn("QR registration skipped for document {} because qrCodeBase64 is null", documentId);
            return;
        }
        String url = integrationProperties.getTracking().getBaseUrl() + "/api/tracking/register";
        RegisterQrPayload payload = new RegisterQrPayload(documentId, qrCodeBase64, registeredBy, Map.of("registeredAt", Instant.now().toString()));
        post(url, payload, "register QR for document " + documentId);
    }

    public void recordSubmission(Long documentId, String ownerUserId) {
        if (!integrationProperties.getTracking().isEmitSubmissionEvents()) {
            log.debug("Submission event emission disabled - skipping tracking event for document {}", documentId);
            return;
        }
        String url = integrationProperties.getTracking().getBaseUrl() + "/api/tracking/scan";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ownerUserId", ownerUserId);
        metadata.put("stage", "SUBMITTED");
        TrackEventPayload payload = new TrackEventPayload(documentId, "DOCUMENT_SUBMITTED", "Document Service", "system", "Document submitted", metadata);
        post(url, payload, "record submission event for document " + documentId);
    }

    private void post(String url, Object payload, String actionDescription) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Void.class);
            log.info("Successfully executed action: {}", actionDescription);
        } catch (Exception ex) {
            log.error("Failed to {}", actionDescription, ex);
        }
    }

    private record RegisterQrPayload(Long documentId, String qrCodeBase64, String registeredBy, Map<String, Object> metadata) {
        private RegisterQrPayload {
            metadata = CollectionUtils.isEmpty(metadata) ? Map.of() : Map.copyOf(metadata);
        }
    }

    private record TrackEventPayload(Long documentId, String eventType, String location, String scannedBy, String notes, Map<String, Object> metadata) {
        private TrackEventPayload {
            metadata = CollectionUtils.isEmpty(metadata) ? Map.of() : Map.copyOf(metadata);
        }
    }
}
