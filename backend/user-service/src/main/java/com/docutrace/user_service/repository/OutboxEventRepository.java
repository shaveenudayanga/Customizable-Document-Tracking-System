package com.docutrace.user_service.repository;

import com.docutrace.user_service.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> { }
