package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.service.NotificationDispatcherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationDispatcherService dispatcherService;

    public NotificationController(NotificationDispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        com.example.notification.model.NotificationEvent entity = dispatcherService.enqueueNotification(request);
        return ResponseEntity.ok().body(new NotificationResponse(entity.getId(), "ENQUEUED"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getStatus(@PathVariable Long id) {
        String status = dispatcherService.getStatus(id);
        return ResponseEntity.ok().body(new NotificationResponse(id, status));
    }
}
