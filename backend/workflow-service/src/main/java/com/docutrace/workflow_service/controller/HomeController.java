package com.docutrace.workflow_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(
                Map.of(
                        "message", "Workflow Service is running. Explore the API under /api/workflow.",
                        "swaggerUi", "/swagger-ui/index.html"
                )
        );
    }
}
