package com.docutrace.tracking_service.service;

import com.docutrace.tracking_service.dto.DocumentHistoryResponse;
import com.docutrace.tracking_service.dto.TrackEventRequest;
import com.docutrace.tracking_service.dto.TrackEventResponse;
import com.docutrace.tracking_service.dto.RegisterQrRequest;
import com.docutrace.tracking_service.entity.TrackingEvent;
import com.docutrace.tracking_service.repository.TrackingEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TrackEventResponse recordEvent(TrackEventRequest request) {
        TrackingEvent event = TrackingEvent.builder()
                .documentId(request.documentId())
                .eventType(request.eventType())
                .location(request.location())
                .scannedBy(request.scannedBy())
                .notes(request.notes())
                .qrCode(request.qrCode())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .metadata(serializeMetadata(request.metadata()))
                .build();

        TrackingEvent saved = eventRepository.save(event);

        return toResponse(saved);
    }

    @Transactional
    public TrackEventResponse registerQr(RegisterQrRequest request) {
        TrackingEvent event = TrackingEvent.builder()
                .documentId(request.documentId())
                .eventType("QR_REGISTERED")
                .location("Document Lifecycle")
                .scannedBy(request.registeredBy())
                .notes("Document QR code registered")
                .qrCode(request.qrCodeBase64())
                .metadata(serializeMetadata(request.metadata()))
                .build();

        TrackingEvent saved = eventRepository.save(event);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DocumentHistoryResponse getHistory(Long documentId) {
        List<TrackingEvent> events = eventRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);

        String currentLocation = events.isEmpty() ? "Unknown" : events.getFirst().getLocation();
        LocalDateTime lastUpdated = events.isEmpty() ? null : events.getFirst().getCreatedAt();

        List<TrackEventResponse> eventResponses = events.stream()
                .map(this::toResponse)
                .toList();

        return new DocumentHistoryResponse(
                documentId,
                events.size(),
                currentLocation,
                lastUpdated,
                eventResponses
        );
    }

    @Transactional(readOnly = true)
    public TrackEventResponse getLatestEvent(Long documentId) {
        TrackingEvent latest = eventRepository.findFirstByDocumentIdOrderByCreatedAtDesc(documentId)
                .orElseThrow(() -> new IllegalArgumentException("No tracking events found for document " + documentId));

                return toResponse(latest);
        }

        private TrackEventResponse toResponse(TrackingEvent entity) {
                return new TrackEventResponse(
                                entity.getId(),
                                entity.getDocumentId(),
                                entity.getEventType(),
                                entity.getLocation(),
                                entity.getScannedBy(),
                                entity.getNotes(),
                                entity.getQrCode(),
                                deserializeMetadata(entity.getMetadata()),
                                entity.getCreatedAt()
                );
        }

        private String serializeMetadata(Map<String, Object> metadata) {
                if (metadata == null || metadata.isEmpty()) {
                        return null;
                }
                try {
                        return objectMapper.writeValueAsString(metadata);
                } catch (JsonProcessingException ex) {
                        throw new IllegalArgumentException("Unable to serialize tracking metadata", ex);
                }
        }

        private Map<String, Object> deserializeMetadata(String metadataJson) {
                if (metadataJson == null || metadataJson.isBlank()) {
                        return Map.of();
                }
                try {
                        return objectMapper.readValue(metadataJson, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                } catch (Exception ex) {
                        return Map.of("_raw", metadataJson);
                }
    }
}
