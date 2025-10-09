package com.soc.notificationservice.notifications.web;

import com.soc.notificationservice.notifications.ApplicationProperties;
import com.soc.notificationservice.notifications.domain.models.DocumentApprovedEvent;
import com.soc.notificationservice.notifications.domain.models.DocumentCreatedEvent;
import com.soc.notificationservice.notifications.domain.models.DocumentErrorEvent;
import com.soc.notificationservice.notifications.domain.models.DocumentRejectedEvent;
import com.soc.notificationservice.notifications.domain.models.DocumentUpdatedEvent;
import com.soc.notificationservice.notifications.domain.models.TaskCompletedEvent;
import com.soc.notificationservice.notifications.domain.models.WorkflowCompletedEvent;
import com.soc.notificationservice.notifications.domain.models.WorkflowRejectedEvent;
import com.soc.notificationservice.notifications.domain.models.WorkflowStartedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class TestEventController {
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationProperties props;

    public TestEventController(RabbitTemplate rabbitTemplate, ApplicationProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @PostMapping("/document-created")
    public Map<String, Object> publishDocumentCreated(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        String documentId = body.getOrDefault("documentId", "DOC-1");
        String title = body.getOrDefault("title", "Sample Document");
        String creator = body.getOrDefault("creator", "system");
        String ownerEmail = body.getOrDefault("ownerEmail", "owner@example.com");
        var event = new DocumentCreatedEvent(eventId, documentId, title, creator, ownerEmail, LocalDateTime.now());
        rabbitTemplate.convertAndSend(props.documentEventsExchange(), props.documentCreatedQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }

    @PostMapping("/document-updated")
    public Map<String, Object> publishDocumentUpdated(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        String documentId = body.getOrDefault("documentId", "DOC-1");
        String title = body.getOrDefault("title", "Sample Document");
        String updater = body.getOrDefault("updater", "editor");
        String ownerEmail = body.getOrDefault("ownerEmail", "owner@example.com");
        var event = new DocumentUpdatedEvent(eventId, documentId, title, updater, ownerEmail, LocalDateTime.now());
        rabbitTemplate.convertAndSend(props.documentEventsExchange(), props.documentUpdatedQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }

    @PostMapping("/document-approved")
    public Map<String, Object> publishDocumentApproved(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        String documentId = body.getOrDefault("documentId", "DOC-1");
        String approver = body.getOrDefault("approver", "manager");
        String ownerEmail = body.getOrDefault("ownerEmail", "owner@example.com");
        var event = new DocumentApprovedEvent(eventId, documentId, approver, ownerEmail, LocalDateTime.now());
        rabbitTemplate.convertAndSend(props.documentEventsExchange(), props.documentApprovedQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }

    @PostMapping("/document-rejected")
    public Map<String, Object> publishDocumentRejected(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        String documentId = body.getOrDefault("documentId", "DOC-1");
        String rejector = body.getOrDefault("rejector", "reviewer");
        String reason = body.getOrDefault("reason", "Insufficient details");
        String ownerEmail = body.getOrDefault("ownerEmail", "owner@example.com");
    var event = new DocumentRejectedEvent(eventId, documentId, rejector, ownerEmail, reason, LocalDateTime.now());
        rabbitTemplate.convertAndSend(props.documentEventsExchange(), props.documentRejectedQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }

    @PostMapping("/document-error")
    public Map<String, Object> publishDocumentError(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        String documentId = body.getOrDefault("documentId", "DOC-1");
        String reason = body.getOrDefault("reason", "Processing failed");
        var event = new DocumentErrorEvent(eventId, documentId, reason, LocalDateTime.now());
        rabbitTemplate.convertAndSend(props.documentEventsExchange(), props.documentErrorQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }

    @PostMapping("/workflow-started")
    public Map<String, Object> publishWorkflowStarted(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        Long documentId = Long.valueOf(body.getOrDefault("documentId", "1"));
        Long pipelineInstanceId = Long.valueOf(body.getOrDefault("pipelineInstanceId", "100"));
        String processInstanceId = body.getOrDefault("processInstanceId", "proc-1");
        String initiator = body.getOrDefault("initiator", "initiatorUser");
        String ownerEmail = body.getOrDefault("ownerEmail", "owner@example.com");
        var event = new WorkflowStartedEvent(eventId, documentId, pipelineInstanceId, processInstanceId, initiator, Instant.now(), ownerEmail);
        rabbitTemplate.convertAndSend(props.workflowEventsExchange(), props.workflowStartedQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }

    @PostMapping("/task-completed")
    public Map<String, Object> publishTaskCompleted(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        Long documentId = Long.valueOf(body.getOrDefault("documentId", "1"));
        Long pipelineInstanceId = Long.valueOf(body.getOrDefault("pipelineInstanceId", "100"));
        String processInstanceId = body.getOrDefault("processInstanceId", "proc-1");
        String taskId = body.getOrDefault("taskId", "task-1");
        String taskName = body.getOrDefault("taskName", "Review");
        Boolean approved = Boolean.valueOf(body.getOrDefault("approved", "true"));
        String completedBy = body.getOrDefault("completedBy", "reviewerUser");
        String notes = body.getOrDefault("notes", "Looks good");
        String ownerEmail = body.getOrDefault("ownerEmail", "owner@example.com");
        var event = new TaskCompletedEvent(eventId, documentId, pipelineInstanceId, processInstanceId, taskId, taskName, approved, completedBy, notes, Instant.now(), ownerEmail);
        rabbitTemplate.convertAndSend(props.workflowEventsExchange(), props.taskCompletedQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }

    @PostMapping("/workflow-completed")
    public Map<String, Object> publishWorkflowCompleted(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        Long documentId = Long.valueOf(body.getOrDefault("documentId", "1"));
        Long pipelineInstanceId = Long.valueOf(body.getOrDefault("pipelineInstanceId", "100"));
        String processInstanceId = body.getOrDefault("processInstanceId", "proc-1");
        String completedBy = body.getOrDefault("completedBy", "system");
        String ownerEmail = body.getOrDefault("ownerEmail", "owner@example.com");
        var event = new WorkflowCompletedEvent(eventId, documentId, pipelineInstanceId, processInstanceId, completedBy, Instant.now(), ownerEmail);
        rabbitTemplate.convertAndSend(props.workflowEventsExchange(), props.workflowCompletedQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }

    @PostMapping("/workflow-rejected")
    public Map<String, Object> publishWorkflowRejected(@RequestBody Map<String, String> body) {
        String eventId = UUID.randomUUID().toString();
        Long documentId = Long.valueOf(body.getOrDefault("documentId", "1"));
        Long pipelineInstanceId = Long.valueOf(body.getOrDefault("pipelineInstanceId", "100"));
        String processInstanceId = body.getOrDefault("processInstanceId", "proc-1");
        String rejectedBy = body.getOrDefault("rejectedBy", "reviewerUser");
        String ownerEmail = body.getOrDefault("ownerEmail", "owner@example.com");
        var event = new WorkflowRejectedEvent(eventId, documentId, pipelineInstanceId, processInstanceId, rejectedBy, Instant.now(), ownerEmail);
        rabbitTemplate.convertAndSend(props.workflowEventsExchange(), props.workflowRejectedQueue(), event);
        return Map.of("status", "published", "eventId", eventId);
    }
}
