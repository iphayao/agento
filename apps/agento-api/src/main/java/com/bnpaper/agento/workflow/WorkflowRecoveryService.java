package com.bnpaper.agento.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * On startup, marks workflows that were stuck in RUNNING/PENDING as FAILED.
 * A workflow is considered stuck if it's been in RUNNING state for over 30 minutes
 * (longer than the max possible agent run time).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowRecoveryService {

    private final AgentWorkflowRepository workflowRepo;

    private static final int STUCK_THRESHOLD_MINUTES = 30;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recoverStuckWorkflows() {
        LocalDateTime stuckBefore = LocalDateTime.now().minusMinutes(STUCK_THRESHOLD_MINUTES);

        List<AgentWorkflow> stuck = workflowRepo
                .findByStatusAndCreatedAtBefore(AgentWorkflowStatus.RUNNING, stuckBefore);

        if (stuck.isEmpty()) {
            log.debug("No stuck workflows found on startup");
            return;
        }

        log.warn("Recovering {} stuck RUNNING workflow(s) older than {} minutes",
                stuck.size(), STUCK_THRESHOLD_MINUTES);

        for (AgentWorkflow wf : stuck) {
            wf.setStatus(AgentWorkflowStatus.FAILED);
            wf.setErrorMessage("Workflow timed out — recovered on server restart");
            workflowRepo.save(wf);
            log.warn("Marked workflow {} as FAILED (was stuck in RUNNING)", wf.getId());
        }
    }
}
