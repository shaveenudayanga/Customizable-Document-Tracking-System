package com.soc.notificationservice.notifications.web;

import com.soc.notificationservice.notifications.domain.DocumentEventEntity;
import com.soc.notificationservice.notifications.domain.DocumentEventRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DocumentEventController {
    private final DocumentEventRepository repository;

    public DocumentEventController(DocumentEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/document-events")
    public List<DocumentEventEntity> list() {
        return repository.findAll();
    }
}
