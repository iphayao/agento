package com.bnpaper.agento.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeneratedContentRepository extends JpaRepository<GeneratedContent, UUID> {

    List<GeneratedContent> findByCampaignIdOrderByCreatedAtDesc(UUID campaignId);

    List<GeneratedContent> findAllByOrderByCreatedAtDesc();

    Optional<GeneratedContent> findFirstByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);
}
