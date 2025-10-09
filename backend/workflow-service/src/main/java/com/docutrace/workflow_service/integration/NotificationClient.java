package com.docutrace.workflow_service.integration;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final boolean enabled;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${user-service.base-url:http://localhost:8081}") String baseUrl,
                              @Value("${notifications.enabled:true}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.enabled = enabled;
    }

    public void sendNotification(String username, String message, String type, Map<String, Object> metadata) {
        if (!enabled) {
            log.debug("Notification feature disabled. Skipping notification for user {}", username);
            return;
        }
        if (username == null || username.isBlank()) {
            log.debug("Username missing. Skipping notification with message '{}'", message);
            return;
        }
        String url = baseUrl + "/api/notifications";
        Map<String, Object> payload = Map.of(
                "username", username,
                "message", message,
                "type", type,
                "metadata", metadata == null ? Map.of() : metadata
        );
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Void.class);
            log.debug("Notification sent to user {}", username);
        } catch (Exception ex) {
            log.error("Failed to send notification to user {}", username, ex);
        }
    }

    private String normalizeBaseUrl(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
