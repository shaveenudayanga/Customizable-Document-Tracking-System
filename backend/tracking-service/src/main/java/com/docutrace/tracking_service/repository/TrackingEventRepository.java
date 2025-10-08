package com.docutrace.tracking_service.repository;

import com.docutrace.tracking_service.entity.TrackingEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByDocumentIdOrderByCreatedAtDesc(Long documentId);

    Optional<TrackingEvent> findFirstByDocumentIdOrderByCreatedAtDesc(Long documentId);

    long countByDocumentId(Long documentId);
}
