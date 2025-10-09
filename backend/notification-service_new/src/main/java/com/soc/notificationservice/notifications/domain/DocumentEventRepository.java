package com.soc.notificationservice.notifications.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentEventRepository extends JpaRepository<DocumentEventEntity, Long> {
    boolean existsByEventId(String eventId);
}
