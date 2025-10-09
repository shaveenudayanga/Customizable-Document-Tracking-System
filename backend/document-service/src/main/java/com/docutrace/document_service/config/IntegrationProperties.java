package com.docutrace.document_service.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    private final Workflow workflow = new Workflow();
    private final Tracking tracking = new Tracking();

    @Data
    @Validated
    public static class Workflow {
        /** Base URL of the workflow service (e.g. http://workflow-service:8083). */
        @NotBlank
        private String baseUrl = "http://localhost:8083";

        /** Whether a workflow should be auto-started after each document submission. */
        private boolean autoStartEnabled = true;

        /** Optional template id to use when triggering the workflow service. */
        @Positive
        private Long defaultTemplateId;
    }

    @Data
    @Validated
    public static class Tracking {
        /** Base URL of the tracking service (e.g. http://tracking-service:8084). */
        @NotBlank
        private String baseUrl = "http://localhost:8084";

        /** Whether QR registrations should be synchronized. */
        private boolean registerQr = true;

        /** Whether submission events should be pushed to the tracking timeline. */
        private boolean emitSubmissionEvents = true;
    }
}
