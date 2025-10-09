package com.docutrace.workflow_service.event;

import com.docutrace.workflow_service.events.WorkflowIntegrationEventPublisher;
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
    private final WorkflowIntegrationEventPublisher workflowIntegrationEventPublisher;

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
        String ownerEmail = (String) event.payload().get("ownerEmail");
        
        trackingServiceClient.recordEvent(event.documentId(), "WORKFLOW_STARTED", "Workflow Engine", initiator, "Workflow started", metadata);
        
        // Publish event to notification service
    workflowIntegrationEventPublisher.publishWorkflowStarted(
            event.documentId(), 
            event.pipelineInstanceId(), 
            event.processInstanceId(), 
            initiator, 
            ownerEmail
        );
    }

    private void onTaskCompleted(WorkflowEvent event, Map<String, Object> metadata) {
        String completedBy = (String) event.payload().get("completedBy");
        String initiator = (String) event.payload().getOrDefault("initiator", completedBy);
        String taskName = (String) event.payload().get("taskName");
        String taskId = (String) event.payload().get("taskId");
        Boolean approved = (Boolean) event.payload().get("approved");
        String notes = (String) event.payload().get("notes");
        String ownerEmail = (String) event.payload().get("ownerEmail");
        String statusNote = Boolean.TRUE.equals(approved) ? "completed" : "marked as rejected";
        String location = Optional.ofNullable((String) event.payload().get("location")).orElse(taskName);

        trackingServiceClient.recordEvent(event.documentId(), "TASK_COMPLETED", location, completedBy, "Task %s %s".formatted(taskName, statusNote), metadata);
        
        // Publish event to notification service
    workflowIntegrationEventPublisher.publishTaskCompleted(
            event.documentId(), 
            taskId, 
            taskName, 
            completedBy, 
            approved, 
            notes, 
            ownerEmail
        );
    }

    private void onWorkflowCompleted(WorkflowEvent event, Map<String, Object> metadata) {
        String completedBy = (String) event.payload().get("completedBy");
        String ownerEmail = (String) event.payload().get("ownerEmail");
        
        trackingServiceClient.recordEvent(event.documentId(), "WORKFLOW_COMPLETED", "Workflow Engine", completedBy, "Workflow completed", metadata);
        
        // Publish event to notification service
    workflowIntegrationEventPublisher.publishWorkflowCompleted(
            event.documentId(), 
            event.pipelineInstanceId(), 
            event.processInstanceId(), 
            completedBy, 
            ownerEmail
        );
    }

    private void onWorkflowRejected(WorkflowEvent event, Map<String, Object> metadata) {
        String rejectedBy = (String) event.payload().get("rejectedBy");
        String ownerEmail = (String) event.payload().get("ownerEmail");
        
        trackingServiceClient.recordEvent(event.documentId(), "WORKFLOW_REJECTED", "Workflow Engine", rejectedBy, "Workflow rejected", metadata);
        
        // Publish event to notification service
    workflowIntegrationEventPublisher.publishWorkflowRejected(
            event.documentId(), 
            event.pipelineInstanceId(), 
            event.processInstanceId(), 
            rejectedBy, 
            ownerEmail
        );
    }
}
