package com.docutrace.document_service.controller;

import java.net.URI;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.docutrace.document_service.dto.FileUploadResponse;
import com.docutrace.document_service.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/documents/{documentId}/files")
@Validated
@Tag(name = "Document Files", description = "Manage document file attachments")
public class DocumentFileController {

    private final DocumentService documentService;

    public DocumentFileController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @Operation(
            summary = "Upload a document file",
            description = "Stores a file on disk and links it to the specified document."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Upload failed due to validation errors"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<FileUploadResponse> uploadDocumentFile(
            @PathVariable Long documentId,
            @Parameter(description = "Binary file to store", required = true)
            @RequestParam("file") @NotNull MultipartFile file) {

        FileUploadResponse response = documentService.uploadFile(documentId, file);

        return ResponseEntity.created(URI.create("/api/documents/" + documentId + "/files/" + response.fileName()))
                .body(response);
    }

    @GetMapping("/{fileName}")
    @Operation(
            summary = "Download a document file",
            description = "Retrieves a previously uploaded file for the provided document identifier and file name."
    )
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "File stream returned"),
                        @ApiResponse(responseCode = "404", description = "Document or file not found")
        })
    public ResponseEntity<Resource> downloadDocumentFile(
            @PathVariable Long documentId,
            @PathVariable String fileName) {

        Resource resource = documentService.downloadFile(documentId, fileName);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(resource.getFilename() == null ? fileName : resource.getFilename())
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }
}
