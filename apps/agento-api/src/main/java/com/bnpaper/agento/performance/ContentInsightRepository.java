package com.bnpaper.agento.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContentInsightRepository extends JpaRepository<ContentInsight, UUID> {

    List<ContentInsight> findAllByOrderByCreatedAtDesc();

    List<ContentInsight> findByInsightTypeOrderByConfidenceScoreDesc(InsightType type);

    List<ContentInsight> findByCampaignIdOrderByCreatedAtDesc(UUID campaignId);
}
