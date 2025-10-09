package com.docutrace.user_service.repository;

import com.docutrace.user_service.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);

    Optional<Notification> findByIdAndUsername(Long id, String username);
}
