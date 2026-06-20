package com.bnpaper.agento.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentWorkflowRepository extends JpaRepository<AgentWorkflow, UUID> {

    List<AgentWorkflow> findByCampaignIdOrderByCreatedAtDesc(UUID campaignId);
}
