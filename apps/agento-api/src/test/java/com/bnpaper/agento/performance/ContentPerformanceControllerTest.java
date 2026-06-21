package com.bnpaper.agento.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bnpaper.agento.security.SecurityConfig;
import com.bnpaper.agento.security.PasswordEncoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContentPerformanceController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
@WithMockUser(roles = "ADMIN")
class ContentPerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContentPerformanceService performanceService;

    @MockBean
    private PerformanceAnalyticsService analyticsService;

    @MockBean
    private PerformanceWorkerClient workerClient;

    private ContentPerformanceDto.Response sampleResponse() {
        return ContentPerformanceDto.Response.builder()
                .id(UUID.randomUUID())
                .generatedContentId(UUID.randomUUID())
                .channel("tiktok")
                .impressions(10_000)
                .clicks(250)
                .orders(15)
                .revenue(new BigDecimal("3750.00"))
                .cost(new BigDecimal("500.00"))
                .engagementRate(new BigDecimal("0.161500"))
                .conversionRate(new BigDecimal("0.060000"))
                .roas(new BigDecimal("7.5000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void GET_performance_returnsOk() throws Exception {
        when(performanceService.findAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].channel").value("tiktok"));
    }

    @Test
    void POST_performance_createsRecord() throws Exception {
        ContentPerformanceDto.Request request = new ContentPerformanceDto.Request();
        request.setGeneratedContentId(UUID.randomUUID());
        request.setChannel("shopee");
        request.setImpressions(5000);
        request.setRevenue(new BigDecimal("1500.00"));
        request.setCost(new BigDecimal("200.00"));

        when(performanceService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void DELETE_performance_id_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(performanceService).delete(id);

        mockMvc.perform(delete("/performance/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void GET_analytics_dashboard_returnsStats() throws Exception {
        ContentPerformanceDto.DashboardStats stats = ContentPerformanceDto.DashboardStats.builder()
                .totalRecords(10)
                .totalRevenue(new BigDecimal("25000.00"))
                .averageRoas(new BigDecimal("5.0000"))
                .averageEngagementRate(new BigDecimal("0.120000"))
                .build();

        when(analyticsService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/performance/analytics/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRecords").value(10));
    }

    @Test
    void GET_insights_returnsInsightList() throws Exception {
        ContentInsightDto.Response insight = ContentInsightDto.Response.builder()
                .id(UUID.randomUUID())
                .insightType(InsightType.WINNING_HOOK)
                .insightText("Questions outperform statements as hooks")
                .confidenceScore(new BigDecimal("0.80"))
                .createdAt(LocalDateTime.now())
                .build();

        when(analyticsService.getInsights()).thenReturn(List.of(insight));

        mockMvc.perform(get("/performance/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].insightType").value("WINNING_HOOK"));
    }

    @Test
    void POST_analyze_withNoData_returnsSkipped() throws Exception {
        when(analyticsService.buildAnalysisPayload(any(), any())).thenReturn(List.of());

        ContentInsightDto.AnalyzeRequest req = new ContentInsightDto.AnalyzeRequest();

        mockMvc.perform(post("/performance/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("skipped"));
    }

    @Test
    void POST_analyze_withData_dispatches() throws Exception {
        when(analyticsService.buildAnalysisPayload(any(), any()))
                .thenReturn(List.of(Map.of("id", UUID.randomUUID().toString())));
        doNothing().when(workerClient).dispatchAnalysis(any(), any());

        ContentInsightDto.AnalyzeRequest req = new ContentInsightDto.AnalyzeRequest();

        mockMvc.perform(post("/performance/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("dispatched"));
    }

    @Test
    void POST_insights_callback_savesInsights() throws Exception {
        PerformanceCallbackPayload payload = new PerformanceCallbackPayload();
        ContentInsightDto.SaveRequest saveReq = new ContentInsightDto.SaveRequest();
        saveReq.setInsightType(InsightType.WINNING_HOOK);
        saveReq.setInsightText("Hook pattern X worked well");
        saveReq.setConfidenceScore(new BigDecimal("0.80"));
        payload.setInsights(List.of(saveReq));

        ContentInsightDto.Response saved = ContentInsightDto.Response.builder()
                .id(UUID.randomUUID())
                .insightType(InsightType.WINNING_HOOK)
                .insightText("Hook pattern X worked well")
                .confidenceScore(new BigDecimal("0.80"))
                .createdAt(LocalDateTime.now())
                .build();

        when(analyticsService.saveInsightsAndFeedKnowledge(any())).thenReturn(List.of(saved));

        mockMvc.perform(post("/performance/insights/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].insightType").value("WINNING_HOOK"));
    }
}
