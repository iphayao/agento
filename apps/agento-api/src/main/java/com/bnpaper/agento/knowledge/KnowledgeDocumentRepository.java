package com.bnpaper.agento.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {
    List<KnowledgeDocument> findAllByOrderByCreatedAtDesc();
    List<KnowledgeDocument> findByTypeAndStatusOrderByCreatedAtDesc(DocumentType type, DocumentStatus status);
}
