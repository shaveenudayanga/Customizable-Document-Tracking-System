package com.docutrace.workflow_service.events;

/**
 * @deprecated Replaced by {@link WorkflowIntegrationEventPublisher}. This placeholder prevents component
 *             scanning conflicts while legacy references are migrated. It is intentionally empty.
 */
@Deprecated(forRemoval = true)
public final class WorkflowEventPublisher {

    private WorkflowEventPublisher() {
        throw new UnsupportedOperationException("Use WorkflowIntegrationEventPublisher instead");
    }
}
