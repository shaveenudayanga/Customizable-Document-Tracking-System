// Controller: HTTP endpoints for CRUD operations on documents and QR retrieval
package com.docutrace.document_service.controller;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.docutrace.document_service.dto.DocumentCreateRequest;
import com.docutrace.document_service.dto.DocumentResponse;
import com.docutrace.document_service.dto.DocumentStatusUpdateRequest;
import com.docutrace.document_service.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documents")
@Validated
@Tag(name = "Documents", description = "Operations that manage document metadata and QR codes")
public class DocumentController {

    // Business service that implements document operations
    private final DocumentService documentService;

    // Constructor - dependencies are injected by Spring
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @Operation(
        summary = "Create a new document",
        description = "Creates a document record, persists it, and generates the associated QR code metadata."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Document created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed for the provided payload")
    })
    // Create a new document record. Validates the request and returns 201 with the created resource.
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentCreateRequest request) {
        DocumentResponse response = documentService.createDocument(request);
        return ResponseEntity.created(URI.create("/api/documents/" + response.id())).body(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create a new document with an initial file",
        description = "Creates a document record and, when a file part is provided, immediately stores it under the new document."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Document created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed for the provided payload")
    })
    public ResponseEntity<DocumentResponse> createDocumentWithFile(
            @Valid @RequestPart("metadata") DocumentCreateRequest metadata,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        DocumentResponse created = documentService.createDocument(metadata);

        if (file != null && !file.isEmpty()) {
            documentService.uploadFile(created.id(), file);
            created = documentService.getDocument(created.id());
        }

        return ResponseEntity.created(URI.create("/api/documents/" + created.id())).body(created);
    }

    // Update Status of a document
    @PostMapping("/{documentId}/status")
    @Operation(
        summary = "Update the status of a document",
        description = "Updates the status field of an existing document."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed for the provided payload"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    // Update a document's statuses. Expects JSON body: { "statuses": ["DEPARTMENT_REVIEWED", "APPROVED"] }
    public ResponseEntity<DocumentResponse> updateDocumentStatus(@PathVariable Long documentId,
                                                                  @Valid @RequestBody DocumentStatusUpdateRequest request) {
    DocumentResponse updated = documentService.updateStatus(documentId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    @Operation(
        summary = "List documents",
        description = "Returns all documents ordered by creation date in descending order."
    )
    // List all documents ordered by creation time (most recent first).
    public List<DocumentResponse> listDocuments() {
        return documentService.listDocuments();
    }

    @GetMapping("/{documentId}")
    @Operation(
        summary = "Get a document",
        description = "Fetch a single document by its identifier."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document found"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    // Retrieve a single document's metadata by ID.
    public DocumentResponse getDocument(@PathVariable Long documentId) {
        return documentService.getDocument(documentId);
    }

    @GetMapping("/{documentId}/qrcode")
    @Operation(
        summary = "Get the QR code for a document",
        description = "Returns the QR code image for a document. When the Accept header requests application/json, the QR code is returned as a Base64 string instead of an image stream."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "QR code returned"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    // Get the QR code for a document. Returns JSON (base64) if requested, otherwise an image stream.
    public ResponseEntity<?> getDocumentQr(
            @PathVariable Long documentId,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String acceptHeader) {

        // Decide whether the client requested JSON (base64) or an image stream for the QR code.
        boolean wantsJson = acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE);
        if (wantsJson) {
            byte[] qrBytes = documentService.getDocumentQrBytes(documentId);
            String base64 = Base64.getEncoder().encodeToString(qrBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "documentId", documentId,
                            "qrCodeBase64", base64
                    ));
        }
        // Return the QR code as an inline PNG resource when the client accepts images.
        Resource resource = documentService.getDocumentQr(documentId);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename("document-" + documentId + "-qr.png")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }
}
