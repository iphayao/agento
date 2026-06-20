package com.bnpaper.agento.performance;

import com.bnpaper.agento.common.dto.ApiResponse;
import com.bnpaper.agento.workflow.WorkerProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
public class ContentPerformanceController {

    private final ContentPerformanceService performanceService;
    private final PerformanceAnalyticsService analyticsService;
    private final PerformanceWorkerClient workerClient;

    // ─── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentPerformanceDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(performanceService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentPerformanceDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.findById(id)));
    }

    @GetMapping("/content/{contentId}")
    public ResponseEntity<ApiResponse<List<ContentPerformanceDto.Response>>> findByContentId(
            @PathVariable UUID contentId) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.findByContentId(contentId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContentPerformanceDto.Response>> create(
            @Valid @RequestBody ContentPerformanceDto.Request request) {
        ContentPerformanceDto.Response response = performanceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Performance record created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentPerformanceDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ContentPerformanceDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        performanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Performance record deleted"));
    }

    // ─── CSV Import ────────────────────────────────────────────────────────────

    @PostMapping("/import/csv")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importCsv(
            @RequestParam("file") MultipartFile file) throws Exception {
        int count = performanceService.importCsv(file);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("imported", count),
                count + " records imported"));
    }

    // ─── Analytics ─────────────────────────────────────────────────────────────

    @GetMapping("/analytics/dashboard")
    public ResponseEntity<ApiResponse<ContentPerformanceDto.DashboardStats>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboardStats()));
    }

    @GetMapping("/analytics/top")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTop(
            @RequestParam(defaultValue = "5") int n) {
        Map<String, Object> top = Map.of(
                "byRevenue", analyticsService.getTopByRevenue(n),
                "byEngagement", analyticsService.getTopByEngagement(n),
                "byRoas", analyticsService.getTopByRoas(n),
                "byChannel", analyticsService.getChannelBreakdown()
        );
        return ResponseEntity.ok(ApiResponse.success(top));
    }

    // ─── Insights ──────────────────────────────────────────────────────────────

    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<List<ContentInsightDto.Response>>> getInsights() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getInsights()));
    }

    @GetMapping("/summaries")
    public ResponseEntity<ApiResponse<List<PerformanceSummaryDto.Response>>> getSummaries() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getSummaries()));
    }

    /**
     * Triggers the Python performance analyst.
     * The worker analyzes content and calls back to POST /performance/insights/callback.
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyze(
            @RequestBody ContentInsightDto.AnalyzeRequest request) {
        List<Map<String, Object>> payload = analyticsService.buildAnalysisPayload(
                request.getTopN(), request.getChannel());

        if (payload.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    Map.of("status", "skipped", "reason", "no performance data"),
                    "No performance data to analyze"));
        }

        workerClient.dispatchAnalysis(payload, request.getChannel());
        return ResponseEntity.accepted()
                .body(ApiResponse.success(
                        Map.of("status", "dispatched", "recordCount", payload.size()),
                        "Analysis dispatched to worker"));
    }

    /** Callback endpoint — called by agento-worker when analysis is complete. */
    @PostMapping("/insights/callback")
    public ResponseEntity<ApiResponse<List<ContentInsightDto.Response>>> receiveInsights(
            @RequestBody PerformanceCallbackPayload payload) {
        List<ContentInsightDto.Response> saved =
                analyticsService.saveInsightsAndFeedKnowledge(payload.getInsights());

        if (payload.getSummary() != null) {
            PerformanceSummary summary = PerformanceSummary.builder()
                    .periodStart(payload.getSummary().getPeriodStart())
                    .periodEnd(payload.getSummary().getPeriodEnd())
                    .channel(payload.getSummary().getChannel())
                    .summaryText(payload.getSummary().getSummaryText())
                    .recommendedAngles(payload.getSummary().getRecommendedAngles())
                    .recommendedHooks(payload.getSummary().getRecommendedHooks())
                    .recommendedCTAs(payload.getSummary().getRecommendedCTAs())
                    .avoidPatterns(payload.getSummary().getAvoidPatterns())
                    .build();
            analyticsService.saveSummary(summary);
        }

        log.info("Received {} insights from worker", saved.size());
        return ResponseEntity.ok(ApiResponse.success(saved, saved.size() + " insights saved"));
    }
}
