package com.docutrace.document_service.dto;

/**
 * Response payload returned after successfully uploading a document file.
 * Contains relative URLs so that upstream gateways can rewrite destinations if needed.
 */
public record FileUploadResponse(
        Long documentId,
        String fileName,
        String downloadUrl,
        String message
) {
}
