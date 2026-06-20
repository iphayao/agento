package com.bnpaper.agento.performance;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ContentPerformanceDto {

    @Data
    public static class Request {
        @NotNull
        private UUID generatedContentId;

        @NotBlank
        private String channel;

        private LocalDateTime publishedAt;

        private long impressions;
        private long views;
        private long clicks;
        private long likes;
        private long comments;
        private long shares;
        private long orders;

        private BigDecimal revenue;
        private BigDecimal cost;
        private String notes;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID generatedContentId;
        private String channel;
        private LocalDateTime publishedAt;
        private long impressions;
        private long views;
        private long clicks;
        private long likes;
        private long comments;
        private long shares;
        private long orders;
        private BigDecimal revenue;
        private BigDecimal conversionRate;
        private BigDecimal engagementRate;
        private BigDecimal cost;
        private BigDecimal roas;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class DashboardStats {
        private long totalRecords;
        private long totalImpressions;
        private long totalClicks;
        private long totalOrders;
        private BigDecimal totalRevenue;
        private BigDecimal totalCost;
        private BigDecimal averageEngagementRate;
        private BigDecimal averageRoas;
    }

    static Response toResponse(ContentPerformance p) {
        return Response.builder()
                .id(p.getId())
                .generatedContentId(p.getGeneratedContentId())
                .channel(p.getChannel())
                .publishedAt(p.getPublishedAt())
                .impressions(p.getImpressions())
                .views(p.getViews())
                .clicks(p.getClicks())
                .likes(p.getLikes())
                .comments(p.getComments())
                .shares(p.getShares())
                .orders(p.getOrders())
                .revenue(p.getRevenue())
                .conversionRate(p.getConversionRate())
                .engagementRate(p.getEngagementRate())
                .cost(p.getCost())
                .roas(p.getRoas())
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
