package com.docutrace.workflow_service.repository;

import com.docutrace.workflow_service.entity.PipelineInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.Optional;

public interface PipelineInstanceRepository extends JpaRepository<PipelineInstance, Long> {
    Optional<PipelineInstance> findByProcessInstanceId(String processInstanceId);
    Optional<PipelineInstance> findFirstByDocumentIdAndStatusInOrderByCreatedAtDesc(Long documentId, Collection<String> statuses);
    Optional<PipelineInstance> findFirstByDocumentIdOrderByCreatedAtDesc(Long documentId);
    Page<PipelineInstance> findByDocumentIdAndStatusIn(Long documentId, Collection<String> statuses, Pageable pageable);
    Page<PipelineInstance> findByDocumentId(Long documentId, Pageable pageable);
    boolean existsByTemplateId(Long templateId);
}
