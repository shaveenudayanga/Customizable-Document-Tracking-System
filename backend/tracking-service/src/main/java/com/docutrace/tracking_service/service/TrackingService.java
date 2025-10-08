package com.docutrace.tracking_service.service;

import com.docutrace.tracking_service.dto.DocumentHistoryResponse;
import com.docutrace.tracking_service.dto.TrackEventRequest;
import com.docutrace.tracking_service.dto.TrackEventResponse;
import com.docutrace.tracking_service.entity.TrackingEvent;
import com.docutrace.tracking_service.repository.TrackingEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackingEventRepository eventRepository;

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
                .build();

        TrackingEvent saved = eventRepository.save(event);

        return new TrackEventResponse(
                saved.getId(),
                saved.getDocumentId(),
                saved.getEventType(),
                saved.getLocation(),
                saved.getScannedBy(),
                saved.getNotes(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public DocumentHistoryResponse getHistory(Long documentId) {
        List<TrackingEvent> events = eventRepository.findByDocumentIdOrderByCreatedAtDesc(documentId);

        String currentLocation = events.isEmpty() ? "Unknown" : events.getFirst().getLocation();
        LocalDateTime lastUpdated = events.isEmpty() ? null : events.getFirst().getCreatedAt();

        List<TrackEventResponse> eventResponses = events.stream()
                .map(e -> new TrackEventResponse(
                        e.getId(),
                        e.getDocumentId(),
                        e.getEventType(),
                        e.getLocation(),
                        e.getScannedBy(),
                        e.getNotes(),
                        e.getCreatedAt()
                ))
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

        return new TrackEventResponse(
                latest.getId(),
                latest.getDocumentId(),
                latest.getEventType(),
                latest.getLocation(),
                latest.getScannedBy(),
                latest.getNotes(),
                latest.getCreatedAt()
        );
    }
}
