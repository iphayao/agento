package com.bnpaper.agento.knowledge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, UUID> {

    List<KnowledgeChunk> findByDocumentIdOrderByChunkIndex(UUID documentId);

    int countByDocumentId(UUID documentId);

    @Modifying
    @Query("DELETE FROM KnowledgeChunk c WHERE c.documentId = :documentId")
    void deleteByDocumentId(@Param("documentId") UUID documentId);

    /**
     * Cosine similarity search using pgvector.
     * The embedding column stores the vector as the pgvector bracket literal "[f1,f2,...]".
     * "<=> " is the cosine distance operator; we return (1 - distance) as score.
     *
     * :docType is optional — pass NULL to search across all active document types.
     */
    @Query(value = """
            SELECT
                kc.id,
                kc.document_id,
                kc.chunk_index,
                kc.chunk_text,
                kc.metadata,
                kd.title  AS doc_title,
                kd.type   AS doc_type,
                1 - (kc.embedding::vector <=> CAST(:queryVector AS vector)) AS score
            FROM knowledge_chunks kc
            JOIN knowledge_documents kd ON kc.document_id = kd.id
            WHERE kd.status = 'ACTIVE'
              AND kc.embedding IS NOT NULL
              AND (:docType IS NULL OR kd.type = :docType)
            ORDER BY kc.embedding::vector <=> CAST(:queryVector AS vector)
            LIMIT :topK
            """, nativeQuery = true)
    List<Object[]> searchSimilar(
            @Param("queryVector") String queryVector,
            @Param("docType") String docType,
            @Param("topK") int topK
    );
}
