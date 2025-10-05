package com.docutrace.document_service.service.exception;

/**
 * Thrown when a requested file cannot be found on disk.
 */
public class FileResourceNotFoundException extends RuntimeException {

    public FileResourceNotFoundException(String message) {
        super(message);
    }

    public FileResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
