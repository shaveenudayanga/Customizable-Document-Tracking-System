package com.docutrace.workflow_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(WorkflowEvent event) {
        log.info("Publishing workflow event: type={}, documentId={}, pipelineInstanceId={}",
                event.type(), event.documentId(), event.pipelineInstanceId());
        publisher.publishEvent(event);
    }
}
