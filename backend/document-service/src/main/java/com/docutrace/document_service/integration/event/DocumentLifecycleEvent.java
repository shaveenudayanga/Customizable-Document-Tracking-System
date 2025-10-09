package com.docutrace.document_service.integration.event;

import com.docutrace.document_service.dto.DocumentResponse;
import java.util.Base64;
import java.util.Objects;

public record DocumentLifecycleEvent(
        DocumentResponse document,
        String qrCodeBase64
) {
    public DocumentLifecycleEvent {
        Objects.requireNonNull(document, "document must not be null");
        qrCodeBase64 = normalizeBase64(qrCodeBase64);
    }

    private static String normalizeBase64(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            Base64.getDecoder().decode(value);
            return value;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
