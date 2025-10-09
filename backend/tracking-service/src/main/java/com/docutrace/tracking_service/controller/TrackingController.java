package com.docutrace.tracking_service.controller;

import com.docutrace.tracking_service.dto.DocumentHistoryResponse;
import com.docutrace.tracking_service.dto.TrackEventRequest;
import com.docutrace.tracking_service.dto.TrackEventResponse;
import com.docutrace.tracking_service.dto.RegisterQrRequest;
import com.docutrace.tracking_service.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/scan")
    public ResponseEntity<TrackEventResponse> recordScan(@Valid @RequestBody TrackEventRequest request) {
        return ResponseEntity.ok(trackingService.recordEvent(request));
    }

    @PostMapping("/register")
    public ResponseEntity<TrackEventResponse> registerQr(@Valid @RequestBody RegisterQrRequest request) {
        return ResponseEntity.ok(trackingService.registerQr(request));
    }

    @GetMapping("/history/{documentId}")
    public ResponseEntity<DocumentHistoryResponse> getHistory(@PathVariable Long documentId) {
        return ResponseEntity.ok(trackingService.getHistory(documentId));
    }

    @GetMapping("/latest/{documentId}")
    public ResponseEntity<TrackEventResponse> getLatestEvent(@PathVariable Long documentId) {
        return ResponseEntity.ok(trackingService.getLatestEvent(documentId));
    }
}
