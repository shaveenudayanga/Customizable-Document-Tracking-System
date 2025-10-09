package com.docutrace.user_service.controller;

import com.docutrace.user_service.dto.NotificationRequest;
import com.docutrace.user_service.dto.NotificationResponse;
import com.docutrace.user_service.service.JwtService;
import com.docutrace.user_service.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@RequestHeader("Authorization") String authorizationHeader) {
        String username = extractUsername(authorizationHeader);
        return ResponseEntity.ok(notificationService.findByUsername(username));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@RequestHeader("Authorization") String authorizationHeader,
                                           @PathVariable Long notificationId) {
        String username = extractUsername(authorizationHeader);
        notificationService.markAsRead(username, notificationId);
        return ResponseEntity.noContent().build();
    }

    private String extractUsername(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String token = header.substring(7);
        return jwtService.extractUsername(token);
    }
}
