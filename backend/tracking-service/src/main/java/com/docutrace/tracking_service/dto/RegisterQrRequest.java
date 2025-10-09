package com.docutrace.tracking_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record RegisterQrRequest(
        @NotNull Long documentId,
        @NotBlank String qrCodeBase64,
        String registeredBy,
        Map<String, Object> metadata
) {}
