package com.soc.notificationservice.notifications.domain;

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
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender emailSender;
    private final ApplicationProperties properties;

    public NotificationService(JavaMailSender emailSender, ApplicationProperties properties) {
        this.emailSender = emailSender;
        this.properties = properties;
    }

    // Order-related notification methods removed as this service now handles
    // document-tracking notifications. Keep document notification methods below.

    public void sendDocumentCreatedNotification(DocumentCreatedEvent event) {
        String message =
                """
                ===================================================
                Document Created Notification
                ----------------------------------------------------
                Dear %s,
                Your document titled '%s' (id: %s) has been created by %s.

                Thanks,
                Document Tracking Team
                ===================================================
                """
                        .formatted(event.ownerEmail(), event.title(), event.documentId(), event.creator());
        log.info("\n{}", message);
        sendEmail(event.ownerEmail(), "Document Created Notification", message);
    }

    public void sendDocumentUpdatedNotification(DocumentUpdatedEvent event) {
        String message =
                """
                ===================================================
                Document Updated Notification
                ----------------------------------------------------
                Dear %s,
                Your document titled '%s' (id: %s) was updated by %s.

                Thanks,
                Document Tracking Team
                ===================================================
                """
                        .formatted(event.ownerEmail(), event.title(), event.documentId(), event.updater());
        log.info("\n{}", message);
        sendEmail(event.ownerEmail(), "Document Updated Notification", message);
    }

    public void sendDocumentApprovedNotification(DocumentApprovedEvent event) {
        String message =
                """
                ===================================================
                Document Approved Notification
                ----------------------------------------------------
                Dear %s,
                Your document (id: %s) was approved by %s.

                Thanks,
                Document Tracking Team
                ===================================================
                """
                        .formatted(event.ownerEmail(), event.documentId(), event.approver());
        log.info("\n{}", message);
        sendEmail(event.ownerEmail(), "Document Approved Notification", message);
    }

    public void sendDocumentRejectedNotification(DocumentRejectedEvent event) {
        String message =
                """
                ===================================================
                Document Rejected Notification
                ----------------------------------------------------
                Dear %s,
                Your document (id: %s) was rejected by %s.
                Reason: %s

                Thanks,
                Document Tracking Team
                ===================================================
                """
                        .formatted(event.ownerEmail(), event.documentId(), event.rejector(), event.reason());
        log.info("\n{}", message);
        sendEmail(event.ownerEmail(), "Document Rejected Notification", message);
    }

    public void sendDocumentErrorEventNotification(DocumentErrorEvent event) {
        String message =
                """
                ===================================================
                Document Processing Failure Notification
                ----------------------------------------------------
                Hi,
                Document processing failed for document id: %s.
                Reason: %s

                Thanks,
                Document Tracking Team
                ===================================================
                """
                        .formatted(event.documentId(), event.reason());
        log.info("\n{}", message);
        sendEmail(properties.supportEmail(), "Document Processing Failure Notification", message);
    }

    // ================= Workflow Notifications =================
    public void sendWorkflowStartedNotification(WorkflowStartedEvent event) {
        String recipient = event.ownerEmail() != null && !event.ownerEmail().isBlank()
                ? event.ownerEmail()
                : properties.supportEmail();
        String message =
                """
        ===================================================
        Workflow Started Notification
        ----------------------------------------------------
        Hello,
        A workflow has been started for document id: %s.
        Initiator: %s
        Pipeline Instance Id: %s
        Process Instance Id: %s

        Thanks,
        Document Tracking Team
        ===================================================
        """
                        .formatted(
                                event.documentId(),
                                event.initiator(),
                                event.pipelineInstanceId(),
                                event.processInstanceId());
        log.info("\n{}", message);
        sendEmail(recipient, "Workflow Started", message);
    }

    public void sendTaskCompletedNotification(TaskCompletedEvent event) {
        String recipient = event.ownerEmail() != null && !event.ownerEmail().isBlank()
                ? event.ownerEmail()
                : properties.supportEmail();
        String decision = Boolean.TRUE.equals(event.approved()) ? "APPROVED" : "REVIEWED";
        String notes = (event.notes() == null || event.notes().isBlank()) ? "N/A" : event.notes();
        String message =
                """
        ===================================================
        Workflow Task Completed Notification
        ----------------------------------------------------
        Hello,
        A workflow task has been completed for document id: %s.
        Task: %s (%s)
        Completed By: %s
        Decision: %s
        Notes: %s

        Thanks,
        Document Tracking Team
        ===================================================
        """
                        .formatted(
                                event.documentId(),
                                event.taskName(),
                                event.taskId(),
                                event.completedBy(),
                                decision,
                                notes);
        log.info("\n{}", message);
        sendEmail(recipient, "Workflow Task Completed", message);
    }

    public void sendWorkflowCompletedNotification(WorkflowCompletedEvent event) {
        String recipient = event.ownerEmail() != null && !event.ownerEmail().isBlank()
                ? event.ownerEmail()
                : properties.supportEmail();
        String message =
                """
        ===================================================
        Workflow Completed Notification
        ----------------------------------------------------
        Hello,
        The workflow has been completed for document id: %s.
        Completed By: %s
        Pipeline Instance Id: %s
        Process Instance Id: %s

        Thanks,
        Document Tracking Team
        ===================================================
        """
                        .formatted(
                                event.documentId(),
                                event.completedBy(),
                                event.pipelineInstanceId(),
                                event.processInstanceId());
        log.info("\n{}", message);
        sendEmail(recipient, "Workflow Completed", message);
    }

    public void sendWorkflowRejectedNotification(WorkflowRejectedEvent event) {
        String recipient = event.ownerEmail() != null && !event.ownerEmail().isBlank()
                ? event.ownerEmail()
                : properties.supportEmail();
        String message =
                """
        ===================================================
        Workflow Rejected Notification
        ----------------------------------------------------
        Hello,
        The workflow has been rejected for document id: %s.
        Rejected By: %s
        Pipeline Instance Id: %s
        Process Instance Id: %s

        Thanks,
        Document Tracking Team
        ===================================================
        """
                        .formatted(
                                event.documentId(),
                                event.rejectedBy(),
                                event.pipelineInstanceId(),
                                event.processInstanceId());
        log.info("\n{}", message);
        sendEmail(recipient, "Workflow Rejected", message);
    }

    private void sendEmail(String recipient, String subject, String content) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(properties.supportEmail());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content);
            emailSender.send(mimeMessage);
            log.info("Email sent to: {}", recipient);
        } catch (Exception e) {
            throw new RuntimeException("Error while sending email", e);
        }
    }
}
