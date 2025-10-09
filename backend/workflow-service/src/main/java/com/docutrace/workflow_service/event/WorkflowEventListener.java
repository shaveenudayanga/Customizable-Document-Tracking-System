package com.docutrace.workflow_service.event;

import com.docutrace.workflow_service.integration.NotificationClient;
import com.docutrace.workflow_service.integration.TrackingServiceClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventListener {

    private final TrackingServiceClient trackingServiceClient;
    private final NotificationClient notificationClient;

    @Async
    @EventListener
    public void handleWorkflowEvent(WorkflowEvent event) {
        Map<String, Object> metadata = new HashMap<>(event.payload());
        metadata.put("pipelineInstanceId", event.pipelineInstanceId());
        metadata.put("processInstanceId", event.processInstanceId());
        metadata.put("eventType", event.type().name());

        switch (event.type()) {
            case WORKFLOW_STARTED -> onWorkflowStarted(event, metadata);
            case TASK_COMPLETED -> onTaskCompleted(event, metadata);
            case WORKFLOW_COMPLETED -> onWorkflowCompleted(event, metadata);
            case WORKFLOW_REJECTED -> onWorkflowRejected(event, metadata);
            default -> log.debug("Unhandled workflow event type: {}", event.type());
        }
    }

    private void onWorkflowStarted(WorkflowEvent event, Map<String, Object> metadata) {
        String initiator = (String) event.payload().get("initiator");
        trackingServiceClient.recordEvent(event.documentId(), "WORKFLOW_STARTED", "Workflow Engine", initiator, "Workflow started", metadata);
        notificationClient.sendNotification(initiator, "Workflow for document %s has started".formatted(event.documentId()), "WORKFLOW_STARTED", metadata);
    }

    private void onTaskCompleted(WorkflowEvent event, Map<String, Object> metadata) {
        String completedBy = (String) event.payload().get("completedBy");
        String initiator = (String) event.payload().getOrDefault("initiator", completedBy);
        String taskName = (String) event.payload().get("taskName");
        Boolean approved = (Boolean) event.payload().get("approved");
        String statusNote = Boolean.TRUE.equals(approved) ? "completed" : "marked as rejected";
        String location = Optional.ofNullable((String) event.payload().get("location")).orElse(taskName);

        trackingServiceClient.recordEvent(event.documentId(), "TASK_COMPLETED", location, completedBy, "Task %s %s".formatted(taskName, statusNote), metadata);
        notificationClient.sendNotification(initiator, "Task '%s' on document %s was %s".formatted(taskName, event.documentId(), statusNote), "TASK_COMPLETED", metadata);
    }

    private void onWorkflowCompleted(WorkflowEvent event, Map<String, Object> metadata) {
        String completedBy = (String) event.payload().get("completedBy");
        trackingServiceClient.recordEvent(event.documentId(), "WORKFLOW_COMPLETED", "Workflow Engine", completedBy, "Workflow completed", metadata);
        notificationClient.sendNotification((String) event.payload().get("initiator"), "Document %s workflow has been completed".formatted(event.documentId()), "WORKFLOW_COMPLETED", metadata);
    }

    private void onWorkflowRejected(WorkflowEvent event, Map<String, Object> metadata) {
        String rejectedBy = (String) event.payload().get("rejectedBy");
        trackingServiceClient.recordEvent(event.documentId(), "WORKFLOW_REJECTED", "Workflow Engine", rejectedBy, "Workflow rejected", metadata);
        notificationClient.sendNotification((String) event.payload().get("initiator"), "Document %s workflow has been rejected".formatted(event.documentId()), "WORKFLOW_REJECTED", metadata);
    }
}
