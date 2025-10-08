package com.soc.notificationservice.notifications.domain;

import com.soc.notificationservice.notifications.ApplicationProperties;
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

    public void sendDocumentCreatedNotification(
            com.sivalabs.bookstore.notifications.domain.models.DocumentCreatedEvent event) {
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

    public void sendDocumentUpdatedNotification(
            com.sivalabs.bookstore.notifications.domain.models.DocumentUpdatedEvent event) {
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

    public void sendDocumentApprovedNotification(
            com.sivalabs.bookstore.notifications.domain.models.DocumentApprovedEvent event) {
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

    public void sendDocumentRejectedNotification(
            com.sivalabs.bookstore.notifications.domain.models.DocumentRejectedEvent event) {
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

    public void sendDocumentErrorEventNotification(
            com.sivalabs.bookstore.notifications.domain.models.DocumentErrorEvent event) {
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
