package com.bnpaper.agento.knowledge;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "knowledge_chunks")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private int chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String chunkText;

    /** pgvector bracket format: "[0.1,0.2,...]". Cast to vector in native SQL search. */
    @Column(columnDefinition = "TEXT")
    private String embedding;

    /** JSON key-value metadata (e.g. {"source": "customer_review", "date": "2024-01"}). */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
