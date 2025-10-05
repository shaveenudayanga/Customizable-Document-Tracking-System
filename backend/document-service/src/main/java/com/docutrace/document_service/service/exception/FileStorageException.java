package com.docutrace.document_service.service.exception;

/**
 * Thrown when a file cannot be stored on disk.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
