package com.docutrace.document_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.docutrace.document_service.entity.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwnerUserId(UUID ownerUserId);

    List<Document> findByDocumentType(String documentType);
}
