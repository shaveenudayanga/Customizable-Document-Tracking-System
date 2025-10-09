package com.soc.notificationservice.notifications.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_events")
public class DocumentEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_event_id_generator")
    @SequenceGenerator(name = "document_event_id_generator", sequenceName = "document_event_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "document_id")
    private String documentId;

    // Store additional payload as JSON or text
    @Column(name = "data", columnDefinition = "text")
    private String data;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DocumentEventEntity() {}

    public DocumentEventEntity(String eventId) {
        this.eventId = eventId;
    }

    public DocumentEventEntity(String eventId, String eventType, String documentId, String data) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.documentId = documentId;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
