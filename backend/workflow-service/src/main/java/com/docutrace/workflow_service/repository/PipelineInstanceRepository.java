package com.docutrace.workflow_service.repository;

import com.docutrace.workflow_service.entity.PipelineInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PipelineInstanceRepository extends JpaRepository<PipelineInstance, Long> {
    Optional<PipelineInstance> findByProcessInstanceId(String processInstanceId);
    Optional<PipelineInstance> findByDocumentId(Long documentId);
}
