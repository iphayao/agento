package com.bnpaper.agento.performance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ContentInsightDto {

    @Data
    public static class SaveRequest {
        private UUID generatedContentId;
        private UUID campaignId;
        private InsightType insightType;
        private String insightText;
        private BigDecimal confidenceScore;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID generatedContentId;
        private UUID campaignId;
        private InsightType insightType;
        private String insightText;
        private BigDecimal confidenceScore;
        private LocalDateTime createdAt;
    }

    @Data
    public static class AnalyzeRequest {
        private List<UUID> contentIds;
        private String channel;
        private Integer topN;
    }

    static Response toResponse(ContentInsight i) {
        return Response.builder()
                .id(i.getId())
                .generatedContentId(i.getGeneratedContentId())
                .campaignId(i.getCampaignId())
                .insightType(i.getInsightType())
                .insightText(i.getInsightText())
                .confidenceScore(i.getConfidenceScore())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
