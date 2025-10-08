package com.soc.notificationservice.notifications.events;

import com.soc.notificationservice.notifications.domain.DocumentEventEntity;
import com.soc.notificationservice.notifications.domain.DocumentEventRepository;
import com.soc.notificationservice.notifications.domain.NotificationService;
import com.soc.notificationservice.notifications.domain.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class WorkflowEventHandler {
    private static final Logger log = LoggerFactory.getLogger(WorkflowEventHandler.class);

    private final NotificationService notificationService;
    private final DocumentEventRepository documentEventRepository;

    public WorkflowEventHandler(
            NotificationService notificationService, DocumentEventRepository documentEventRepository) {
        this.notificationService = notificationService;
        this.documentEventRepository = documentEventRepository;
    }

    @RabbitListener(queues = "${notification.workflow-started-queue}")
    public void handle(WorkflowStartedEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate WorkflowStartedEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received WorkflowStartedEvent for documentId:{}", event.documentId());
        notificationService.sendWorkflowStartedNotification(event);
        documentEventRepository.save(new DocumentEventEntity(event.eventId()));
    }

    @RabbitListener(queues = "${notification.task-completed-queue}")
    public void handle(TaskCompletedEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate TaskCompletedEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received TaskCompletedEvent for documentId:{}", event.documentId());
        notificationService.sendTaskCompletedNotification(event);
        documentEventRepository.save(new DocumentEventEntity(event.eventId()));
    }

    @RabbitListener(queues = "${notification.workflow-completed-queue}")
    public void handle(WorkflowCompletedEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate WorkflowCompletedEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received WorkflowCompletedEvent for documentId:{}", event.documentId());
        notificationService.sendWorkflowCompletedNotification(event);
        documentEventRepository.save(new DocumentEventEntity(event.eventId()));
    }

    @RabbitListener(queues = "${notification.workflow-rejected-queue}")
    public void handle(WorkflowRejectedEvent event) {
        if (documentEventRepository.existsByEventId(event.eventId())) {
            log.warn("Received duplicate WorkflowRejectedEvent with eventId: {}", event.eventId());
            return;
        }
        log.info("Received WorkflowRejectedEvent for documentId:{}", event.documentId());
        notificationService.sendWorkflowRejectedNotification(event);
        documentEventRepository.save(new DocumentEventEntity(event.eventId()));
    }
}
