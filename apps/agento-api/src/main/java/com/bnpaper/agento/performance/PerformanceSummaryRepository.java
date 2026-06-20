package com.bnpaper.agento.performance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PerformanceSummaryRepository extends JpaRepository<PerformanceSummary, UUID> {

    List<PerformanceSummary> findAllByOrderByCreatedAtDesc();

    List<PerformanceSummary> findByChannelOrderByCreatedAtDesc(String channel);
}
