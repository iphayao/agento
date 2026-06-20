package com.bnpaper.agento.performance;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentPerformanceServiceTest {

    @Mock
    private ContentPerformanceRepository repo;

    @InjectMocks
    private ContentPerformanceService service;

    private UUID contentId;
    private UUID perfId;
    private ContentPerformance samplePerf;

    @BeforeEach
    void setUp() {
        contentId = UUID.randomUUID();
        perfId = UUID.randomUUID();
        samplePerf = ContentPerformance.builder()
                .id(perfId)
                .generatedContentId(contentId)
                .channel("tiktok")
                .impressions(10_000)
                .views(8_500)
                .clicks(250)
                .likes(1_200)
                .comments(45)
                .shares(120)
                .orders(15)
                .revenue(new BigDecimal("3750.00"))
                .cost(new BigDecimal("500.00"))
                .build();
    }

    @Test
    void computeMetrics_engagementRate_calculatedFromImpressions() {
        service.computeMetrics(samplePerf);

        // (likes + comments + shares + clicks) / impressions
        // (1200 + 45 + 120 + 250) / 10000 = 0.1615
        assertThat(samplePerf.getEngagementRate())
                .isEqualByComparingTo(new BigDecimal("0.161500"));
    }

    @Test
    void computeMetrics_conversionRate_calculatedFromClicks() {
        service.computeMetrics(samplePerf);

        // orders / clicks = 15 / 250 = 0.06
        assertThat(samplePerf.getConversionRate())
                .isEqualByComparingTo(new BigDecimal("0.060000"));
    }

    @Test
    void computeMetrics_roas_calculatedFromRevenueAndCost() {
        service.computeMetrics(samplePerf);

        // 3750 / 500 = 7.5
        assertThat(samplePerf.getRoas())
                .isEqualByComparingTo(new BigDecimal("7.5000"));
    }

    @Test
    void computeMetrics_zeroCost_roasIsNull() {
        samplePerf.setCost(BigDecimal.ZERO);
        service.computeMetrics(samplePerf);
        assertThat(samplePerf.getRoas()).isNull();
    }

    @Test
    void computeMetrics_zeroImpressions_usesViewsForEngagement() {
        samplePerf.setImpressions(0);
        samplePerf.setViews(8_500);
        service.computeMetrics(samplePerf);

        // engagement uses views as fallback
        // (1200 + 45 + 120 + 250) / 8500
        assertThat(samplePerf.getEngagementRate()).isPositive();
    }

    @Test
    void computeMetrics_zeroImpressions_zeroViews_engagementIsZero() {
        samplePerf.setImpressions(0);
        samplePerf.setViews(0);
        service.computeMetrics(samplePerf);
        assertThat(samplePerf.getEngagementRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void create_savesAndComputesMetrics() {
        ContentPerformanceDto.Request request = new ContentPerformanceDto.Request();
        request.setGeneratedContentId(contentId);
        request.setChannel("shopee");
        request.setImpressions(5000);
        request.setClicks(100);
        request.setOrders(10);
        request.setRevenue(new BigDecimal("2000.00"));
        request.setCost(new BigDecimal("200.00"));

        when(repo.save(any())).thenAnswer(inv -> {
            ContentPerformance p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ContentPerformanceDto.Response response = service.create(request);

        verify(repo).save(any(ContentPerformance.class));
        assertThat(response.getChannel()).isEqualTo("shopee");
        assertThat(response.getRoas()).isNotNull();
    }

    @Test
    void findById_notFound_throwsException() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_notFound_throwsException() {
        when(repo.existsById(any())).thenReturn(false);
        assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_returnsAllSortedByDate() {
        when(repo.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(samplePerf));
        List<ContentPerformanceDto.Response> all = service.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getChannel()).isEqualTo("tiktok");
    }
}
