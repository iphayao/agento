package com.bnpaper.agento.knowledge;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSearchResult {

    private String query;
    private List<ChunkMatch> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkMatch {
        private UUID chunkId;
        private UUID documentId;
        private String documentTitle;
        private String documentType;
        private String chunkText;
        private double score;
        private int chunkIndex;
    }
}
