package com.bnpaper.agento.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AgentWorkflowRepository extends JpaRepository<AgentWorkflow, UUID> {

    List<AgentWorkflow> findByCampaignIdOrderByCreatedAtDesc(UUID campaignId);

    List<AgentWorkflow> findByStatusAndCreatedAtBefore(AgentWorkflowStatus status, LocalDateTime before);

    boolean existsByCampaignIdAndStatusIn(UUID campaignId, List<AgentWorkflowStatus> statuses);
}
