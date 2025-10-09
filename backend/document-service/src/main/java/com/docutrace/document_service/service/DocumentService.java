// Service: core business logic for creating/listing documents, file handling, and QR codes
package com.docutrace.document_service.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.docutrace.document_service.config.StorageProperties;
import com.docutrace.document_service.dto.DocumentCreateRequest;
import com.docutrace.document_service.dto.DocumentResponse;
import com.docutrace.document_service.dto.DocumentStatusUpdateRequest;
import com.docutrace.document_service.dto.FileUploadResponse;
import com.docutrace.document_service.entity.Document;
import com.docutrace.document_service.mapper.DocumentMapper;
import com.docutrace.document_service.repository.DocumentRepository;
import com.docutrace.document_service.service.exception.DocumentNotFoundException;
import com.docutrace.document_service.service.exception.FileResourceNotFoundException;
import com.docutrace.document_service.service.exception.FileStorageException;
import com.docutrace.document_service.integration.event.DocumentLifecycleEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class DocumentService {

    private static final List<String> DEFAULT_STATUSES = List.of("DEPARTMENT_PENDING", "APPROVAL_PENDING");
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final FileStorageService fileStorageService;
    private final QrCodeService qrCodeService;
    private final Path baseStoragePath;
    private final ApplicationEventPublisher eventPublisher;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentMapper documentMapper,
                           FileStorageService fileStorageService,
                           QrCodeService qrCodeService,
                           StorageProperties storageProperties,
                           ApplicationEventPublisher eventPublisher) {
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
        this.fileStorageService = fileStorageService;
        this.qrCodeService = qrCodeService;
        this.baseStoragePath = storageProperties.getBasePath().toAbsolutePath().normalize();
        this.eventPublisher = eventPublisher;
    }

    public DocumentResponse createDocument(DocumentCreateRequest request) {
        Document document = documentMapper.toEntity(request);
        document.setStatus(encodeStatuses(DEFAULT_STATUSES));
        Document persisted = documentRepository.save(document);

        Path documentDirectory = baseStoragePath.resolve(String.valueOf(persisted.getId()));
        Path filesDirectory = documentDirectory.resolve("files");
        ensureDirectoryExists(filesDirectory);

        Path qrPath = qrCodeService.generateDocumentQr(persisted.getId(), documentDirectory);
        persisted.setQrPath(qrPath.toString());
        persisted.setFileDir(filesDirectory.toString());

    Document updated = documentRepository.save(persisted);
    DocumentResponse response = toResponse(updated);
    publishDocumentCreatedEvent(response, qrPath);
    return response;
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long documentId) {
        return toResponse(findDocument(documentId));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FileUploadResponse uploadFile(Long documentId, MultipartFile file) {
        Document document = findDocument(documentId);
        Path storedPath = fileStorageService.storeFile(documentId, file);

        document.setFileDir(storedPath.getParent().toString());
        documentRepository.save(document);

    String downloadUrl = buildFileDownloadUrl(documentId, storedPath.getFileName().toString());
    return new FileUploadResponse(
        documentId,
        storedPath.getFileName().toString(),
        downloadUrl,
        "File uploaded successfully"
    );
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(Long documentId, String filename) {
        findDocument(documentId);
        return fileStorageService.loadFileAsResource(documentId, filename);
    }

    @Transactional
    public Resource getDocumentQr(Long documentId) {
        Path qrPath = resolveQrPath(documentId);
        return toResource(qrPath, "QR code");
    }

    @Transactional
    public byte[] getDocumentQrBytes(Long documentId) {
        Path qrPath = resolveQrPath(documentId);
        try {
            return Files.readAllBytes(qrPath);
        } catch (IOException ex) {
            throw new FileStorageException("Unable to read QR code for document " + documentId, ex);
        }
    }

    private Document findDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    private void ensureDirectoryExists(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new FileStorageException("Failed to create directory: " + directory, ex);
        }
    }

    private DocumentResponse toResponse(Document document) {
    DocumentResponse base = documentMapper.toResponse(document);
    String qrUrl = buildQrUrl(document.getId());
    String filesUrl = buildFilesUrl(document.getId());
    List<String> statuses = base.statuses() == null ? List.of() : List.copyOf(base.statuses());
    return new DocumentResponse(
        base.id(),
        base.title(),
        base.documentType(),
        base.description(),
        base.ownerUserId(),
        statuses,
        base.processInstanceId(),
        qrUrl,
        filesUrl,
        base.createdAt(),
        base.updatedAt()
    );
    }

    private void publishDocumentCreatedEvent(DocumentResponse response, Path qrPath) {
        try {
            byte[] qrBytes = Files.readAllBytes(qrPath);
            String qrBase64 = Base64.getEncoder().encodeToString(qrBytes);
            eventPublisher.publishEvent(new DocumentLifecycleEvent(response, qrBase64));
        } catch (IOException ex) {
            log.warn("Unable to read QR code bytes for document {}. Integration event will omit QR payload", response.id(), ex);
            eventPublisher.publishEvent(new DocumentLifecycleEvent(response, null));
        } catch (Exception ex) {
            log.error("Failed to publish document lifecycle event for document {}", response.id(), ex);
        }
    }

    private String buildQrUrl(Long documentId) {
        return "/api/documents/" + documentId + "/qrcode";
    }

    private String buildFilesUrl(Long documentId) {
        return "/api/documents/" + documentId + "/files";
    }

    private String buildFileDownloadUrl(Long documentId, String filename) {
        String encodedName = UriUtils.encodePathSegment(filename, StandardCharsets.UTF_8);
        return buildFilesUrl(documentId) + "/" + encodedName;
    }

    private Path resolveQrPath(Long documentId) {
        Document document = findDocument(documentId);
        Path qrPath = null;
        if (document.getQrPath() != null && !document.getQrPath().isBlank()) {
            qrPath = Path.of(document.getQrPath());
        }

        if (qrPath == null || !Files.exists(qrPath)) {
            Path documentDirectory = baseStoragePath.resolve(String.valueOf(documentId));
            qrPath = qrCodeService.generateDocumentQr(documentId, documentDirectory);
            document.setQrPath(qrPath.toString());
            documentRepository.save(document);
        }

        return qrPath;
    }

    private Resource toResource(Path path, String description) {
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
        } catch (IOException ex) {
            throw new FileStorageException("Unable to load " + description + " from " + path, ex);
        }
        throw new FileResourceNotFoundException(description + " not found at path: " + path);
    }

    /**
     * Update the department and approval statuses of an existing document.
     */
    public DocumentResponse updateStatus(Long documentId, DocumentStatusUpdateRequest request) {
        Document document = findDocument(documentId);
        document.setStatus(encodeStatuses(request.statuses()));
        String processInstanceId = request.processInstanceId();
        if (processInstanceId != null && !processInstanceId.isBlank()) {
            document.setProcessInstanceId(processInstanceId.trim());
        }
        Document saved = documentRepository.save(document);
        return toResponse(saved);
    }

    private String encodeStatuses(List<String> statuses) {
        List<String> safeStatuses = Objects.requireNonNullElse(statuses, List.of());
        if (safeStatuses.isEmpty()) {
            return "";
        }
        if (safeStatuses.size() != 2) {
            throw new IllegalArgumentException("Exactly two statuses (department and approval) are required");
        }
        return safeStatuses.stream()
                .map(status -> status == null ? "" : status.trim())
                .collect(Collectors.joining("|"));
    }
}
