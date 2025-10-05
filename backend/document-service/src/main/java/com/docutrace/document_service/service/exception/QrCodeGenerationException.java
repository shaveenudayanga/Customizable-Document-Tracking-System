package com.docutrace.document_service.service.exception;

/**
 * Runtime exception thrown when QR code generation fails.
 */
public class QrCodeGenerationException extends RuntimeException {

    public QrCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
