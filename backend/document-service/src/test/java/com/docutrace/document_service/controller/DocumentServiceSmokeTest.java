package com.docutrace.document_service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.docutrace.document_service.dto.DocumentCreateRequest;
import com.docutrace.document_service.dto.DocumentResponse;
import com.docutrace.document_service.dto.FileUploadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
class DocumentServiceSmokeTest {

        private static final Path STORAGE_BASE_PATH = initStoragePath();
        private static final String SMOKE_DB_NAME = "docservice-smoke-" + UUID.randomUUID();
    private static final byte[] SAMPLE_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/P+ZWDwAAAABJRU5ErkJggg==");

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.base-path", () -> STORAGE_BASE_PATH.toString());
        registry.add("spring.datasource.url", () ->
                "jdbc:h2:mem:" + SMOKE_DB_NAME + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;" +
                        "DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Test
    void documentLifecycleSmokeTest() throws Exception {
        // POST create document
        DocumentCreateRequest createRequest = new DocumentCreateRequest(
                "Smoke Test Document",
                "REPORT",
                "Verifying full lifecycle",
                UUID.randomUUID(),
                null,
                null
        );

        ResponseEntity<DocumentResponse> createResponse = restTemplate.postForEntity(
                "/api/documents",
                createRequest,
                DocumentResponse.class
        );

        assertThat(createResponse.getStatusCode().value()).isEqualTo(201);
        DocumentResponse createdDocument = Objects.requireNonNull(createResponse.getBody());
        Long documentId = createdDocument.id();
        assertThat(documentId).isNotNull();
        assertThat(createdDocument.qrPath()).isEqualTo("/api/documents/" + documentId + "/qrcode");
        assertThat(createdDocument.fileDir()).isEqualTo("/api/documents/" + documentId + "/files");

        // GET document by id
        ResponseEntity<DocumentResponse> getResponse = restTemplate.getForEntity(
                "/api/documents/" + documentId,
                DocumentResponse.class
        );
        assertThat(getResponse.getStatusCode().value()).isEqualTo(200);
        DocumentResponse fetched = Objects.requireNonNull(getResponse.getBody());
        assertThat(fetched.title()).isEqualTo(createRequest.title());
        assertThat(fetched.documentType()).isEqualTo(createRequest.documentType());
        assertThat(fetched.description()).isEqualTo(createRequest.description());
        assertThat(fetched.ownerUserId()).isEqualTo(createRequest.ownerUserId());
        assertThat(fetched.statuses()).containsExactly("DEPARTMENT_PENDING", "APPROVAL_PENDING");
        assertThat(fetched.qrPath()).isEqualTo(createdDocument.qrPath());
        assertThat(fetched.fileDir()).isEqualTo("/api/documents/" + documentId + "/files");
        assertThat(fetched.createdAt()).isNotNull();
        assertThat(fetched.updatedAt()).isNotNull();

        // GET QR code
        HttpHeaders qrHeaders = new HttpHeaders();
        qrHeaders.setAccept(List.of(MediaType.IMAGE_PNG));
        ResponseEntity<byte[]> qrResponse = restTemplate.exchange(
                createdDocument.qrPath(),
                HttpMethod.GET,
                new HttpEntity<>(qrHeaders),
                byte[].class
        );
        assertThat(qrResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(qrResponse.getHeaders().getContentType()).isNotNull()
                .satisfies(ct -> assertThat(ct.isCompatibleWith(MediaType.IMAGE_PNG)).isTrue());
        assertThat(qrResponse.getBody()).isNotNull().isNotEmpty();

        // Upload file
        String fileName = "smoke-test.png";
        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("file", buildFilePart(fileName, SAMPLE_PNG, MediaType.IMAGE_PNG));

        HttpHeaders multipartHeaders = new HttpHeaders();
        multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<FileUploadResponse> uploadResponse = restTemplate.exchange(
                "/api/documents/" + documentId + "/files",
                HttpMethod.POST,
                new HttpEntity<>(multipartBody, multipartHeaders),
                FileUploadResponse.class
        );

        assertThat(uploadResponse.getStatusCode().value()).isEqualTo(201);
        FileUploadResponse uploadBody = Objects.requireNonNull(uploadResponse.getBody());
        assertThat(uploadBody.fileName()).isEqualTo(fileName);
        assertThat(uploadBody.downloadUrl()).isEqualTo("/api/documents/" + documentId + "/files/" + fileName);

        Path storedFile = STORAGE_BASE_PATH
                .resolve(String.valueOf(documentId))
                .resolve("files")
                .resolve(fileName);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).isEqualTo(SAMPLE_PNG);

        // Download file
        ResponseEntity<byte[]> downloadResponse = restTemplate.exchange(
                "/api/documents/" + documentId + "/files/" + fileName,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                byte[].class
        );

        assertThat(downloadResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(downloadResponse.getHeaders().getContentType()).isNotNull()
                .satisfies(ct -> assertThat(
                        ct.isCompatibleWith(MediaType.IMAGE_PNG) || MediaType.APPLICATION_OCTET_STREAM.equals(ct))
                        .isTrue());
        assertThat(Objects.requireNonNull(downloadResponse.getBody())).isEqualTo(SAMPLE_PNG);

        // Swagger UI availability
        ResponseEntity<String> swaggerResponse = restTemplate.getForEntity(
                "/swagger-ui/index.html",
                String.class
        );
        assertThat(swaggerResponse.getStatusCode().value()).isEqualTo(200);
        assertThat(swaggerResponse.getBody()).isNotNull();
        assertThat(swaggerResponse.getBody()).contains("Swagger UI");

        // Optional: ensure openapi spec reachable
        ResponseEntity<String> openApiResponse = restTemplate.getForEntity(
                "/openapi",
                String.class
        );
        int docsStatus = openApiResponse.getStatusCode().value();
        assertThat(docsStatus >= 200 && docsStatus < 400).isTrue();
        if (openApiResponse.getStatusCode().is2xxSuccessful()) {
            assertThat(Objects.requireNonNull(openApiResponse.getBody())).isNotBlank();
        }
    }

    @Test
    void createDocument_withFileInSingleRequest() throws Exception {
        UUID ownerId = UUID.randomUUID();
        DocumentCreateRequest metadata = new DocumentCreateRequest(
                "Single Call Document",
                "REPORT",
                "Metadata and file in one shot",
                ownerId,
                null,
                null
        );

        ObjectMapper mapper = new ObjectMapper();
        String metadataJson = mapper.writeValueAsString(metadata);

        HttpHeaders metadataHeaders = new HttpHeaders();
        metadataHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> metadataPart = new HttpEntity<>(metadataJson, metadataHeaders);

        String fileName = "inline-upload.png";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("metadata", metadataPart);
        body.add("file", buildFilePart(fileName, SAMPLE_PNG, MediaType.IMAGE_PNG));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<DocumentResponse> response = restTemplate.exchange(
                "/api/documents",
                HttpMethod.POST,
                new HttpEntity<>(body, requestHeaders),
                DocumentResponse.class
        );

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        DocumentResponse created = Objects.requireNonNull(response.getBody());
        assertThat(created.id()).isNotNull();
        assertThat(created.ownerUserId()).isEqualTo(ownerId);
        assertThat(created.statuses()).containsExactly("DEPARTMENT_PENDING", "APPROVAL_PENDING");

        Path storedFile = STORAGE_BASE_PATH
                .resolve(String.valueOf(created.id()))
                .resolve("files")
                .resolve(fileName);
        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readAllBytes(storedFile)).isEqualTo(SAMPLE_PNG);
    }

    @Test
    void createDocument_withValidationErrorsReturnsBadRequest() {
        DocumentCreateRequest invalidRequest = new DocumentCreateRequest(
                "",
                "",
                "",
                null,
                null,
                null
        );

                ResponseEntity<String> response = restTemplate.postForEntity(
                                "/api/documents",
                                invalidRequest,
                                String.class
                );

                assertThat(response.getStatusCode().value()).isEqualTo(400);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).contains("validationErrors");
    }

    private static HttpEntity<Resource> buildFilePart(String filename, byte[] content, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("file", filename);
        Resource resource = new NamedByteArrayResource(filename, content);
        return new HttpEntity<>(resource, headers);
    }

    private static Path initStoragePath() {
        try {
            return Files.createTempDirectory("doc-service-storage");
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create temp storage directory", ex);
        }
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        NamedByteArrayResource(String filename, byte[] byteArray) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
