package com.bnpaper.agento.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentStepResultRepository extends JpaRepository<AgentStepResult, UUID> {

    List<AgentStepResult> findByWorkflowIdOrderByStartedAtAsc(UUID workflowId);

    void deleteByWorkflowId(UUID workflowId);
}
