package com.bnpaper.agento.performance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ContentPerformanceRepository extends JpaRepository<ContentPerformance, UUID> {

    List<ContentPerformance> findByGeneratedContentId(UUID generatedContentId);

    List<ContentPerformance> findAllByOrderByCreatedAtDesc();

    List<ContentPerformance> findByChannelOrderByCreatedAtDesc(String channel);

    @Query("SELECT p FROM ContentPerformance p ORDER BY p.revenue DESC")
    List<ContentPerformance> findTopByRevenue();

    @Query("SELECT p FROM ContentPerformance p ORDER BY p.engagementRate DESC NULLS LAST")
    List<ContentPerformance> findTopByEngagement();

    @Query("SELECT p FROM ContentPerformance p ORDER BY p.roas DESC NULLS LAST")
    List<ContentPerformance> findTopByRoas();

    @Query("SELECT p.channel, COUNT(p), SUM(p.impressions), SUM(p.clicks), SUM(p.revenue) " +
           "FROM ContentPerformance p GROUP BY p.channel")
    List<Object[]> aggregateByChannel();
}
