package com.docutrace.workflow_service.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class WorkflowEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String workflowEventsExchange;

    public WorkflowEventPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${workflow.events.exchange:workflow-events-exchange}") String workflowEventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.workflowEventsExchange = workflowEventsExchange;
    }

    public void publishWorkflowStarted(Long documentId, Long pipelineInstanceId, String processInstanceId, 
                                        String initiator, String ownerEmail) {
        WorkflowStartedEvent event = new WorkflowStartedEvent(
                UUID.randomUUID().toString(),
                documentId,
                pipelineInstanceId,
                processInstanceId,
                initiator,
                Instant.now(),
                ownerEmail
        );
        
        String routingKey = "workflow.started";
        publishEvent(event, routingKey);
    }

    public void publishTaskCompleted(Long documentId, String taskId, String taskName, 
                                     String completedBy, Boolean approved, String notes, String ownerEmail) {
        TaskCompletedEvent event = new TaskCompletedEvent(
                UUID.randomUUID().toString(),
                documentId,
                taskId,
                taskName,
                completedBy,
                approved,
                notes,
                Instant.now(),
                ownerEmail
        );
        
        String routingKey = "workflow.task.completed";
        publishEvent(event, routingKey);
    }

    public void publishWorkflowCompleted(Long documentId, Long pipelineInstanceId, String processInstanceId,
                                         String completedBy, String ownerEmail) {
        WorkflowCompletedEvent event = new WorkflowCompletedEvent(
                UUID.randomUUID().toString(),
                documentId,
                pipelineInstanceId,
                processInstanceId,
                completedBy,
                Instant.now(),
                ownerEmail
        );
        
        String routingKey = "workflow.completed";
        publishEvent(event, routingKey);
    }

    public void publishWorkflowRejected(Long documentId, Long pipelineInstanceId, String processInstanceId,
                                        String rejectedBy, String ownerEmail) {
        WorkflowRejectedEvent event = new WorkflowRejectedEvent(
                UUID.randomUUID().toString(),
                documentId,
                pipelineInstanceId,
                processInstanceId,
                rejectedBy,
                Instant.now(),
                ownerEmail
        );
        
        String routingKey = "workflow.rejected";
        publishEvent(event, routingKey);
    }

    private void publishEvent(Object event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(workflowEventsExchange, routingKey, event);
            log.info("Published event to exchange '{}' with routing key '{}': {}", 
                    workflowEventsExchange, routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish event to exchange '{}' with routing key '{}': {}", 
                    workflowEventsExchange, routingKey, event, e);
        }
    }
}
