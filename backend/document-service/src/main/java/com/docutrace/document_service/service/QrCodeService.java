package com.docutrace.document_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

import com.docutrace.document_service.config.StorageProperties;
import com.docutrace.document_service.service.exception.QrCodeGenerationException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrCodeService {

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;
    private static final String IMAGE_FORMAT = "PNG";
    private static final String DOCUMENT_LINK_TEMPLATE = "/documents/%d";
    private static final String DEFAULT_QR_FILE_NAME = "qr.png";

    private final StorageProperties storageProperties;
    private final QRCodeWriter qrCodeWriter = new QRCodeWriter();

    public QrCodeService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    /**
     * Generates a QR code for the provided document identifier and stores it in the given directory.
     *
     * @param documentId      document identifier
     * @param targetDirectory directory where the qr.png file should be stored
     * @return absolute {@link Path} to the generated QR image
     */
    public Path generateDocumentQr(Long documentId, Path targetDirectory) {
        Path outputPath = resolveOutputPath(targetDirectory);
        String content = buildDocumentLink(documentId);

        try {
            Files.createDirectories(outputPath.getParent());
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
            MatrixToImageWriter.writeToPath(bitMatrix, IMAGE_FORMAT, outputPath);
            return outputPath;
        } catch (WriterException | IOException ex) {
            throw new QrCodeGenerationException("Failed to generate QR code for document " + documentId, ex);
        }
    }

    /**
     * Generates a QR code within the default document directory under the configured base path.
     */
    public Path generateDocumentQr(Long documentId) {
        Path documentDirectory = storageProperties.getBasePath().resolve(String.valueOf(documentId));
        return generateDocumentQr(documentId, documentDirectory);
    }

    private String buildDocumentLink(Long documentId) {
        return String.format(DOCUMENT_LINK_TEMPLATE, documentId);
    }

    private Path resolveOutputPath(Path targetDirectory) {
        Path directory = targetDirectory == null
                ? storageProperties.getBasePath()
                : targetDirectory.toAbsolutePath().normalize();
        return directory.resolve(DEFAULT_QR_FILE_NAME);
    }
}
