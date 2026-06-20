package com.bnpaper.agento.performance;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PerformanceSummaryDto {

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private String channel;
        private String summaryText;
        private List<String> recommendedAngles;
        private List<String> recommendedHooks;
        private List<String> recommendedCTAs;
        private List<String> avoidPatterns;
        private LocalDateTime createdAt;
    }

    static Response toResponse(PerformanceSummary s) {
        return Response.builder()
                .id(s.getId())
                .periodStart(s.getPeriodStart())
                .periodEnd(s.getPeriodEnd())
                .channel(s.getChannel())
                .summaryText(s.getSummaryText())
                .recommendedAngles(s.getRecommendedAngles())
                .recommendedHooks(s.getRecommendedHooks())
                .recommendedCTAs(s.getRecommendedCTAs())
                .avoidPatterns(s.getAvoidPatterns())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
