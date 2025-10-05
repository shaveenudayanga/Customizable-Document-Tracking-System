package com.docutrace.document_service.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.docutrace.document_service.config.StorageProperties;

class QrCodeServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void generateDocumentQr_createsPngFile() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.setBasePath(tempDir);

        QrCodeService qrCodeService = new QrCodeService(properties);

    Path qrPath = qrCodeService.generateDocumentQr(42L, tempDir.resolve("42"));

        assertTrue(Files.exists(qrPath), "QR code file should exist");
        assertTrue(Files.size(qrPath) > 0, "QR code file should not be empty");
        assertTrue(qrPath.getFileName().toString().endsWith(".png"), "QR code file should be a PNG");
    assertTrue(qrPath.getParent().endsWith("42"), "QR code should be stored inside the document directory");
    }
}
