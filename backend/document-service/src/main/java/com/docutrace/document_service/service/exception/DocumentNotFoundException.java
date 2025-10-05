// Exception: thrown when a requested document cannot be found
package com.docutrace.document_service.service.exception;

/**
 * Thrown when the requested document metadata cannot be located.
 */
public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long documentId) {
        super("Document not found with id: " + documentId);
    }
}
