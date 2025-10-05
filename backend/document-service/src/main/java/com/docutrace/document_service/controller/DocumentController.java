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
import org.springframework.web.bind.annotation.RestController;

import com.docutrace.document_service.dto.DocumentCreateRequest;
import com.docutrace.document_service.dto.DocumentResponse;
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

    private final DocumentService documentService;

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
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody DocumentCreateRequest request) {
        DocumentResponse response = documentService.createDocument(request);
        return ResponseEntity.created(URI.create("/api/documents/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(
        summary = "List documents",
        description = "Returns all documents ordered by creation date in descending order."
    )
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
    public ResponseEntity<?> getDocumentQr(
            @PathVariable Long documentId,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String acceptHeader) {

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
