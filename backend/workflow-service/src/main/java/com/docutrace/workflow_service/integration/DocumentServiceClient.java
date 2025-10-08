package com.docutrace.workflow_service.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DocumentServiceClient {

	private final RestTemplate restTemplate;
	private final String baseUrl;

	public DocumentServiceClient(RestTemplate restTemplate,
								 @Value("${document-service.base-url}") String baseUrl) {
		this.restTemplate = restTemplate;
		this.baseUrl = normalizeBaseUrl(baseUrl);
	}

	public DocumentSnapshot getDocument(Long documentId) {
		Objects.requireNonNull(documentId, "documentId must not be null");
		String url = baseUrl + "/api/documents/" + documentId;
		return restTemplate.getForObject(url, DocumentSnapshot.class);
	}

	public void updateDocumentStatus(Long documentId, List<String> statuses, String processInstanceId) {
		Objects.requireNonNull(documentId, "documentId must not be null");
		Objects.requireNonNull(statuses, "statuses must not be null");
		String url = baseUrl + "/api/documents/" + documentId + "/status";
		restTemplate.postForEntity(url, new DocumentStatusUpdatePayload(statuses, processInstanceId), Void.class);
	}

	private String normalizeBaseUrl(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("document-service.base-url property must be configured");
		}
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record DocumentSnapshot(Long id, List<String> statuses, String processInstanceId) {}

	public record DocumentStatusUpdatePayload(List<String> statuses, String processInstanceId) {}
}
