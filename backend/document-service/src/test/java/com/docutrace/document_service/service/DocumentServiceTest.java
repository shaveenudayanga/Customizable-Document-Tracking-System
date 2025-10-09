package com.docutrace.document_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.context.ApplicationEventPublisher;

import com.docutrace.document_service.config.StorageProperties;
import com.docutrace.document_service.dto.DocumentCreateRequest;
import com.docutrace.document_service.dto.DocumentResponse;
import com.docutrace.document_service.dto.DocumentStatusUpdateRequest;
import com.docutrace.document_service.dto.FileUploadResponse;
import com.docutrace.document_service.entity.Document;
import com.docutrace.document_service.mapper.DocumentMapper;
import com.docutrace.document_service.repository.DocumentRepository;
import com.docutrace.document_service.integration.event.DocumentLifecycleEvent;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DocumentMapper documentMapper;
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentMapper = Mappers.getMapper(DocumentMapper.class);
        StorageProperties properties = new StorageProperties();
        properties.setBasePath(tempDir);
    documentService = new DocumentService(documentRepository, documentMapper, fileStorageService, qrCodeService, properties, eventPublisher);
    }

    @Test
    void createDocument_persistsMetadataAndGeneratesQr() throws Exception {
        DocumentCreateRequest request = new DocumentCreateRequest(
                "Doc Title",
                "REPORT",
                "Summary",
                UUID.randomUUID(),
                null,
                null
        );

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            if (doc.getId() == null) {
                doc.setId(1L);
            }
            return doc;
        });

        Path expectedQrPath = tempDir.resolve("1").resolve("qr.png");
        when(qrCodeService.generateDocumentQr(eq(1L), any(Path.class))).thenReturn(expectedQrPath);

        DocumentResponse response = documentService.createDocument(request);

        assertEquals(1L, response.id());
        assertEquals("Doc Title", response.title());
        assertEquals("/api/documents/1/qrcode", response.qrPath());
        assertEquals("/api/documents/1/files", response.fileDir());
        assertEquals(List.of("DEPARTMENT_PENDING", "APPROVAL_PENDING"), response.statuses());
        assertTrue(Files.exists(tempDir.resolve("1/files")));

        ArgumentCaptor<Path> qrDirectoryCaptor = ArgumentCaptor.forClass(Path.class);
        verify(qrCodeService).generateDocumentQr(eq(1L), qrDirectoryCaptor.capture());
        assertTrue(qrDirectoryCaptor.getValue().endsWith("1"));

    verify(eventPublisher).publishEvent(any(DocumentLifecycleEvent.class));
    }

    @Test
    void uploadFile_delegatesToStorageAndUpdatesMetadata() {
        Document document = new Document();
        document.setId(10L);
        when(documentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "evidence.pdf",
                "application/pdf",
                "data".getBytes()
        );

        Path storedPath = tempDir.resolve("10/files/evidence.pdf");
        when(fileStorageService.storeFile(10L, multipartFile)).thenReturn(storedPath);

        FileUploadResponse response = documentService.uploadFile(10L, multipartFile);

        assertEquals("evidence.pdf", response.fileName());
        assertEquals("/api/documents/10/files/evidence.pdf", response.downloadUrl());
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void downloadFile_returnsResource() {
        Document document = new Document();
        document.setId(5L);
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));

        Resource resource = new ByteArrayResource("content".getBytes());
        when(fileStorageService.loadFileAsResource(5L, "file.pdf")).thenReturn(resource);

        Resource result = documentService.downloadFile(5L, "file.pdf");

        assertEquals(resource, result);
    }

    @Test
    void getDocumentQr_returnsExistingResource() throws Exception {
        Path qrPath = tempDir.resolve("15").resolve("qr.png");
        Files.createDirectories(qrPath.getParent());
        Files.writeString(qrPath, "qr");

        Document document = new Document();
        document.setId(15L);
        document.setQrPath(qrPath.toString());

        when(documentRepository.findById(15L)).thenReturn(Optional.of(document));

        Resource resource = documentService.getDocumentQr(15L);

        assertTrue(resource.exists());
        assertEquals(qrPath.toUri(), resource.getURI());
    }

    @Test
    void updateStatus_persistsBothStatuses() {
        Document document = new Document();
        document.setId(42L);
        document.setStatus("DEPARTMENT_PENDING|APPROVAL_PENDING");

        when(documentRepository.findById(42L)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<String> newStatuses = List.of("DEPARTMENT_REVIEWED", "APPROVED");
    DocumentStatusUpdateRequest request = new DocumentStatusUpdateRequest(newStatuses, "PROC-123");

    DocumentResponse response = documentService.updateStatus(42L, request);

        assertEquals(newStatuses, response.statuses());
    assertEquals("DEPARTMENT_REVIEWED|APPROVED", document.getStatus());
    assertEquals("PROC-123", document.getProcessInstanceId());
    assertEquals("PROC-123", response.processInstanceId());
    }
}
