package com.bnpaper.agento.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedContentRepository extends JpaRepository<GeneratedContent, Long> {

    List<GeneratedContent> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);

    List<GeneratedContent> findAllByOrderByCreatedAtDesc();
}
