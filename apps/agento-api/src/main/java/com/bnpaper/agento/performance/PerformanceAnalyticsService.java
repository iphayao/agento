package com.bnpaper.agento.performance;

import com.bnpaper.agento.content.GeneratedContent;
import com.bnpaper.agento.content.GeneratedContentRepository;
import com.bnpaper.agento.knowledge.DocumentStatus;
import com.bnpaper.agento.knowledge.DocumentType;
import com.bnpaper.agento.knowledge.KnowledgeDocument;
import com.bnpaper.agento.knowledge.KnowledgeDocumentService;
import com.bnpaper.agento.knowledge.KnowledgeDocumentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceAnalyticsService {

    private static final int TOP_N = 5;

    private final ContentPerformanceRepository perfRepo;
    private final ContentInsightRepository insightRepo;
    private final PerformanceSummaryRepository summaryRepo;
    private final GeneratedContentRepository contentRepo;
    private final KnowledgeDocumentService knowledgeService;

    public ContentPerformanceDto.DashboardStats getDashboardStats() {
        List<ContentPerformance> all = perfRepo.findAll();
        if (all.isEmpty()) {
            return ContentPerformanceDto.DashboardStats.builder()
                    .totalRecords(0).totalImpressions(0).totalClicks(0)
                    .totalOrders(0).totalRevenue(BigDecimal.ZERO).totalCost(BigDecimal.ZERO)
                    .averageEngagementRate(BigDecimal.ZERO).averageRoas(BigDecimal.ZERO)
                    .build();
        }

        long totalImpressions = all.stream().mapToLong(ContentPerformance::getImpressions).sum();
        long totalClicks = all.stream().mapToLong(ContentPerformance::getClicks).sum();
        long totalOrders = all.stream().mapToLong(ContentPerformance::getOrders).sum();
        BigDecimal totalRevenue = all.stream().map(p -> nvl(p.getRevenue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = all.stream().map(p -> nvl(p.getCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgEngagement = all.stream()
                .filter(p -> p.getEngagementRate() != null)
                .map(ContentPerformance::getEngagementRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(all.size()), 6, RoundingMode.HALF_UP);

        BigDecimal avgRoas = totalCost.compareTo(BigDecimal.ZERO) > 0
                ? totalRevenue.divide(totalCost, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ContentPerformanceDto.DashboardStats.builder()
                .totalRecords(all.size())
                .totalImpressions(totalImpressions)
                .totalClicks(totalClicks)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalCost(totalCost)
                .averageEngagementRate(avgEngagement)
                .averageRoas(avgRoas)
                .build();
    }

    public List<ContentPerformanceDto.Response> getTopByRevenue(int n) {
        return perfRepo.findTopByRevenue().stream()
                .limit(n)
                .map(ContentPerformanceDto::toResponse)
                .toList();
    }

    public List<ContentPerformanceDto.Response> getTopByEngagement(int n) {
        return perfRepo.findTopByEngagement().stream()
                .limit(n)
                .map(ContentPerformanceDto::toResponse)
                .toList();
    }

    public List<ContentPerformanceDto.Response> getTopByRoas(int n) {
        return perfRepo.findTopByRoas().stream()
                .limit(n)
                .map(ContentPerformanceDto::toResponse)
                .toList();
    }

    public List<Map<String, Object>> getChannelBreakdown() {
        return perfRepo.aggregateByChannel().stream().map(row -> Map.<String, Object>of(
                "channel", row[0].toString(),
                "count", ((Number) row[1]).longValue(),
                "impressions", ((Number) row[2]).longValue(),
                "clicks", ((Number) row[3]).longValue(),
                "revenue", row[4]
        )).toList();
    }

    public List<ContentInsightDto.Response> getInsights() {
        return insightRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(ContentInsightDto::toResponse)
                .toList();
    }

    public List<PerformanceSummaryDto.Response> getSummaries() {
        return summaryRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(PerformanceSummaryDto::toResponse)
                .toList();
    }

    /**
     * Persists a list of AI-generated insights and feeds high-confidence ones
     * into the knowledge base as WINNING_CONTENT or MARKET_INSIGHT documents.
     */
    @Transactional
    public List<ContentInsightDto.Response> saveInsightsAndFeedKnowledge(
            List<ContentInsightDto.SaveRequest> requests) {

        List<ContentInsightDto.Response> saved = new ArrayList<>();
        for (ContentInsightDto.SaveRequest req : requests) {
            ContentInsight insight = ContentInsight.builder()
                    .generatedContentId(req.getGeneratedContentId())
                    .campaignId(req.getCampaignId())
                    .insightType(req.getInsightType())
                    .insightText(req.getInsightText())
                    .confidenceScore(req.getConfidenceScore() != null
                            ? req.getConfidenceScore() : BigDecimal.ZERO)
                    .build();
            insight = insightRepo.save(insight);
            saved.add(ContentInsightDto.toResponse(insight));

            // Feed high-confidence positive insights to the knowledge base
            if (isHighConfidence(insight) && isPositiveInsight(insight.getInsightType())) {
                feedToKnowledge(insight);
            }
        }
        return saved;
    }

    @Transactional
    public PerformanceSummaryDto.Response saveSummary(PerformanceSummary summary) {
        summary = summaryRepo.save(summary);
        return PerformanceSummaryDto.toResponse(summary);
    }

    /** Build a worker payload from the n most recent performance records */
    public List<Map<String, Object>> buildAnalysisPayload(Integer topN, String channel) {
        List<ContentPerformance> records = channel != null && !channel.isBlank()
                ? perfRepo.findByChannelOrderByCreatedAtDesc(channel)
                : perfRepo.findAllByOrderByCreatedAtDesc();

        int limit = topN != null ? topN : 50;
        return records.stream().limit(limit).map(p -> {
            GeneratedContent gc = contentRepo.findById(p.getGeneratedContentId()).orElse(null);
            return Map.<String, Object>of(
                    "id", p.getId().toString(),
                    "channel", p.getChannel(),
                    "hook", gc != null && gc.getHook() != null ? gc.getHook() : "",
                    "callToAction", gc != null && gc.getCallToAction() != null ? gc.getCallToAction() : "",
                    "title", gc != null && gc.getTitle() != null ? gc.getTitle() : "",
                    "engagementRate", p.getEngagementRate() != null ? p.getEngagementRate() : BigDecimal.ZERO,
                    "conversionRate", p.getConversionRate() != null ? p.getConversionRate() : BigDecimal.ZERO,
                    "revenue", nvl(p.getRevenue()),
                    "roas", p.getRoas() != null ? p.getRoas() : BigDecimal.ZERO,
                    "orders", p.getOrders()
            );
        }).toList();
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private void feedToKnowledge(ContentInsight insight) {
        DocumentType docType = switch (insight.getInsightType()) {
            case WINNING_HOOK, WINNING_ANGLE, STRONG_CTA -> DocumentType.WINNING_CONTENT;
            case AUDIENCE_SIGNAL, CHANNEL_SIGNAL -> DocumentType.MARKET_INSIGHT;
            default -> null;
        };
        if (docType == null) return;

        String title = switch (insight.getInsightType()) {
            case WINNING_HOOK -> "Performance Insight: Winning Hook";
            case WINNING_ANGLE -> "Performance Insight: Winning Angle";
            case STRONG_CTA -> "Performance Insight: Strong CTA";
            case AUDIENCE_SIGNAL -> "Performance Insight: Audience Signal";
            case CHANNEL_SIGNAL -> "Performance Insight: Channel Signal";
            default -> "Performance Insight";
        };

        KnowledgeDocumentDto.Request req = new KnowledgeDocumentDto.Request();
        req.setTitle(title);
        req.setType(docType);
        req.setContent(insight.getInsightText());
        req.setSource("performance-analyst");
        req.setTags(List.of("auto-generated", "performance", insight.getInsightType().name().toLowerCase()));

        try {
            knowledgeService.create(req);
            log.info("Fed insight {} ({}) into knowledge base as {}",
                    insight.getId(), insight.getInsightType(), docType);
        } catch (Exception e) {
            log.warn("Failed to feed insight {} into knowledge base: {}", insight.getId(), e.getMessage());
        }
    }

    private static boolean isHighConfidence(ContentInsight insight) {
        return insight.getConfidenceScore().compareTo(new BigDecimal("0.65")) >= 0;
    }

    private static boolean isPositiveInsight(InsightType type) {
        return switch (type) {
            case WINNING_HOOK, WINNING_ANGLE, STRONG_CTA, AUDIENCE_SIGNAL, CHANNEL_SIGNAL -> true;
            case LOW_PERFORMING_ANGLE, WEAK_CTA -> false;
        };
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
