package com.docutrace.document_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.docutrace.document_service.config.StorageProperties;
import com.docutrace.document_service.service.exception.FileStorageException;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeFile_persistsFileAndReturnsPath() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.setBasePath(tempDir);
        FileStorageService service = new FileStorageService(properties);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "sample.pdf",
                "application/pdf",
                "hello world".getBytes()
        );

        Path storedPath = service.storeFile(123L, multipartFile);

        assertTrue(Files.exists(storedPath));
        assertEquals("sample.pdf", storedPath.getFileName().toString());
        assertTrue(storedPath.toString().contains("123"));
        assertEquals("hello world", Files.readString(storedPath));
    }

    @Test
    void storeFile_rejectsUnsupportedContentType() {
        StorageProperties properties = new StorageProperties();
        properties.setBasePath(tempDir);
        FileStorageService service = new FileStorageService(properties);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "sample.txt",
                "text/plain",
                "disallowed".getBytes()
        );

        assertThrows(FileStorageException.class, () -> service.storeFile(123L, multipartFile));
    }
}
