package com.bnpaper.agento.knowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class KnowledgeDocumentDto {

    @Data
    @NoArgsConstructor
    public static class Request {
        @NotBlank(message = "Title is required")
        @Size(max = 500, message = "Title must be under 500 characters")
        private String title;

        @NotNull(message = "Document type is required")
        private DocumentType type;

        @NotBlank(message = "Content is required")
        private String content;

        private String source;
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String title;
        private DocumentType type;
        private String content;
        private String source;
        private List<String> tags;
        private DocumentStatus status;
        private int chunkCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkResponse {
        private UUID id;
        private UUID documentId;
        private int chunkIndex;
        private String chunkText;
        private boolean hasEmbedding;
        private LocalDateTime createdAt;
    }

    static Response toResponse(KnowledgeDocument doc, int chunkCount) {
        return Response.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .type(doc.getType())
                .content(doc.getContent())
                .source(doc.getSource())
                .tags(doc.getTags())
                .status(doc.getStatus())
                .chunkCount(chunkCount)
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    static ChunkResponse toChunkResponse(KnowledgeChunk chunk) {
        return ChunkResponse.builder()
                .id(chunk.getId())
                .documentId(chunk.getDocumentId())
                .chunkIndex(chunk.getChunkIndex())
                .chunkText(chunk.getChunkText())
                .hasEmbedding(chunk.getEmbedding() != null)
                .createdAt(chunk.getCreatedAt())
                .build();
    }
}
