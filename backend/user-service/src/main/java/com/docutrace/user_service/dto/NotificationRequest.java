package com.docutrace.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record NotificationRequest(
        @NotBlank @Size(max = 128) String username,
        @NotBlank @Size(max = 64) String type,
        @NotBlank @Size(max = 512) String message,
        Map<String, Object> metadata
) {}
