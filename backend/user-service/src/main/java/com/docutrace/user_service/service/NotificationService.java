package com.docutrace.user_service.service;

import com.docutrace.user_service.dto.NotificationRequest;
import com.docutrace.user_service.dto.NotificationResponse;
import com.docutrace.user_service.entity.Notification;
import com.docutrace.user_service.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUsername(request.username());
        notification.setType(request.type());
        notification.setMessage(request.message());
        notification.setMetadata(serializeMetadata(request.metadata()));

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByUsername(String username) {
        return notificationRepository.findByUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(String username, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUsername(notificationId, username)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.isReadFlag()) {
            notification.setReadFlag(true);
            notificationRepository.save(notification);
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isReadFlag(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                deserializeMetadata(notification.getMetadata())
        );
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize notification metadata", ex);
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
