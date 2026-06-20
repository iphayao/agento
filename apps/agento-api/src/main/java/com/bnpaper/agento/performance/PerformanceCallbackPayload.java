package com.bnpaper.agento.performance;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PerformanceCallbackPayload {

    private List<ContentInsightDto.SaveRequest> insights;
    private SummaryPayload summary;

    @Data
    public static class SummaryPayload {
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private String channel;
        private String summaryText;
        private List<String> recommendedAngles;
        private List<String> recommendedHooks;
        private List<String> recommendedCTAs;
        private List<String> avoidPatterns;
    }
}
