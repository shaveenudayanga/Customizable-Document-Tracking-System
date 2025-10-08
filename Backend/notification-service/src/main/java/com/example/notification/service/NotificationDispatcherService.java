package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.model.NotificationEvent;
import com.example.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class NotificationDispatcherService {

    private final NotificationRepository repository;
    private final MailSenderService mailSenderService;
    private final SmsSenderService smsSenderService;
    private final BackgroundProcessor backgroundProcessor;

    public NotificationDispatcherService(NotificationRepository repository,
                                         MailSenderService mailSenderService,
                                         SmsSenderService smsSenderService,
                                         BackgroundProcessor backgroundProcessor) {
        this.repository = repository;
        this.mailSenderService = mailSenderService;
        this.smsSenderService = smsSenderService;
        this.backgroundProcessor = backgroundProcessor;
    }

    public NotificationEvent enqueueNotification(NotificationRequest request) {
        NotificationEvent ev = new NotificationEvent();
        ev.setChannel(request.getChannel().name());
        ev.setRecipient(request.getRecipient());
        ev.setSubject(request.getSubject());
        ev.setMessage(request.getMessage());
        ev.setStatus("ENQUEUED");
        ev.setCreatedAt(OffsetDateTime.now());
        final NotificationEvent savedEv = repository.save(ev);

        // schedule async processing
        backgroundProcessor.submit(() -> process(savedEv.getId()));
        return savedEv;
    }

    public String getStatus(Long id) {
        return repository.findById(id).map(NotificationEvent::getStatus).orElse("NOT_FOUND");
    }

    public void process(Long id) {
        repository.findById(id).ifPresent(ev -> {
            try {
                if ("EMAIL".equals(ev.getChannel())) {
                    mailSenderService.sendEmail(ev.getRecipient(), ev.getSubject(), ev.getMessage());
                } else if ("SMS".equals(ev.getChannel())) {
                    smsSenderService.sendSms(ev.getRecipient(), ev.getMessage());
                }
                ev.setStatus("SENT");
            } catch (Exception ex) {
                ev.setStatus("FAILED: " + ex.getMessage());
            }
            ev.setProcessedAt(OffsetDateTime.now());
            repository.save(ev);
        });
    }
}
