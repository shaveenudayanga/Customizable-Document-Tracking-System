package com.soc.notificationservice.notifications.web;

import com.soc.notificationservice.notifications.domain.DocumentEventEntity;
import com.soc.notificationservice.notifications.domain.DocumentEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DocumentEventController {
    private final DocumentEventRepository repository;
    private final ObjectMapper objectMapper;

    public DocumentEventController(DocumentEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/document-events")
    public List<DocumentEventEntity> list() {
        return repository.findAll();
    }

    @PostMapping("/document-events")
    public ResponseEntity<?> create(@Valid @RequestBody SaveEventRequest request) {
        String eventId = request.eventId();
        if (eventId == null || eventId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "eventId is required"));
        }
        if (repository.existsByEventId(eventId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Event with eventId already exists", "eventId", eventId));
        }

        String dataJson = null;
        if (request.data() != null) {
            try {
                dataJson = objectMapper.writeValueAsString(request.data());
            } catch (JsonProcessingException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid data payload: must be valid JSON"));
            }
        }

        DocumentEventEntity toSave = new DocumentEventEntity(
                eventId,
                request.eventType(),
                request.documentId(),
                dataJson);
        DocumentEventEntity saved = repository.save(toSave);
        return ResponseEntity.created(URI.create("/api/document-events"))
                .body(Map.of(
                        "id", saved.getId(),
                        "eventId", saved.getEventId(),
                        "eventType", saved.getEventType(),
                        "documentId", saved.getDocumentId()));
    }

    public static record SaveEventRequest(@NotBlank String eventId, String eventType, String documentId, Object data) {}
}
