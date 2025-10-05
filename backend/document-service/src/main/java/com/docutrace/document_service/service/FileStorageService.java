package com.docutrace.document_service.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.docutrace.document_service.config.StorageProperties;
import com.docutrace.document_service.service.exception.FileResourceNotFoundException;
import com.docutrace.document_service.service.exception.FileStorageException;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    private final Path basePath;

    public FileStorageService(StorageProperties storageProperties) {
        this.basePath = storageProperties.getBasePath().toAbsolutePath().normalize();
    }

    /**
     * Stores the provided multipart file under the document directory and returns the stored file path.
     *
     * @param documentId identifier of the document the file belongs to
     * @param file       multipart file payload
     * @return absolute path of the stored file
     */
    public Path storeFile(Long documentId, MultipartFile file) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileStorageException("File name is required");
        }
        originalFilename = StringUtils.cleanPath(originalFilename);
        if (originalFilename.isBlank()) {
            throw new FileStorageException("File name is required");
        }
        if (originalFilename.contains("..")) {
            throw new FileStorageException("File name contains invalid path sequence: " + originalFilename);
        }

        Path documentDir = getDocumentFilesDirectory(documentId);
        Path targetLocation = documentDir.resolve(originalFilename).normalize();

        try {
            Files.createDirectories(documentDir);
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return targetLocation;
        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file " + originalFilename, ex);
        }
    }

    /**
     * Loads a file resource for streaming to clients.
     *
     * @param documentId identifier of the owner document
     * @param filename   name of the file to load
     * @return resource pointing to the stored file
     */
    public Resource loadFileAsResource(Long documentId, String filename) {
        if (filename == null || filename.isBlank()) {
            throw new FileResourceNotFoundException("File name is required");
        }
        String sanitized = StringUtils.cleanPath(filename);
        if (sanitized.contains("..")) {
            throw new FileResourceNotFoundException("Invalid file path");
        }

        Path filePath = getDocumentFilesDirectory(documentId).resolve(sanitized).normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new FileResourceNotFoundException("File not found: " + sanitized);
        } catch (IOException ex) {
            throw new FileResourceNotFoundException("Unable to read file: " + sanitized, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Uploaded file is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageException("File exceeds the maximum allowed size of " + MAX_FILE_SIZE_BYTES + " bytes");
        }
        String contentType = file.getContentType();
        if (contentType == null || ALLOWED_CONTENT_TYPES.stream().noneMatch(contentType::equalsIgnoreCase)) {
            throw new FileStorageException("File type is not supported: " + contentType);
        }
    }

    private Path getDocumentFilesDirectory(Long documentId) {
        return basePath.resolve(String.valueOf(documentId)).resolve("files").normalize();
    }
}
