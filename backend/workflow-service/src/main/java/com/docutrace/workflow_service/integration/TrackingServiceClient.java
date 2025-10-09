package com.docutrace.workflow_service.integration;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class TrackingServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TrackingServiceClient(RestTemplate restTemplate,
                                 @org.springframework.beans.factory.annotation.Value("${tracking-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    public void recordEvent(Long documentId,
                            String eventType,
                            String location,
                            String actor,
                            String notes,
                            Map<String, Object> metadata) {
    String url = baseUrl + "/api/tracking/scan";
        Map<String, Object> payload = new HashMap<>();
        payload.put("documentId", documentId);
        payload.put("eventType", eventType);
        payload.put("location", location);
        payload.put("scannedBy", actor);
        payload.put("notes", notes);
        payload.put("metadata", CollectionUtils.isEmpty(metadata) ? Map.of() : metadata);
        post(url, payload, "record tracking event %s for document %s".formatted(eventType, documentId));
    }

    private void post(String url, Map<String, Object> payload, String description) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Void.class);
            log.debug("Completed integration call: {}", description);
        } catch (Exception ex) {
            log.error("Failed to {}", description, ex);
        }
    }

    private String normalizeBaseUrl(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
