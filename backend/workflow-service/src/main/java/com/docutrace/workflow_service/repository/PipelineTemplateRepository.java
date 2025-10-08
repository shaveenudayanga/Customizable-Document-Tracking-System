package com.docutrace.workflow_service.repository;

import com.docutrace.workflow_service.entity.PipelineTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PipelineTemplateRepository extends JpaRepository<PipelineTemplate, Long> {
    Optional<PipelineTemplate> findByDocumentTypeAndIsPermanent(String docType, Boolean isPermanent);
    List<PipelineTemplate> findByIsPermanent(Boolean isPermanent);
}
