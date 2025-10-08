// Config: file storage properties (base path)
package com.docutrace.document_service.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for storage-related settings.
 */
@Validated
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * Base directory where document-related assets (files, QR codes) are stored.
     */
    @NotNull
    private Path basePath = Path.of("/data/docs");

    public Path getBasePath() {
        return basePath;
    }

    public void setBasePath(Path basePath) {
        this.basePath = basePath;
    }
}
