package com.bnpaper.agento.workflow;

import com.bnpaper.agento.audit.AuditAction;
import com.bnpaper.agento.audit.AuditService;
import com.bnpaper.agento.common.dto.ApiResponse;
import com.bnpaper.agento.common.ratelimit.AiRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AgentWorkflowController {

    private final AgentWorkflowService service;
    private final AuditService auditService;
    private final AiRateLimiter rateLimiter;

    @PostMapping("/campaigns/{campaignId}/agent-workflows")
    public ResponseEntity<ApiResponse<AgentWorkflowDto.Response>> create(
            @PathVariable UUID campaignId,
            Authentication auth,
            HttpServletRequest httpReq) {

        rateLimiter.consume(resolveKey(auth, httpReq));

        AgentWorkflowDto.Response res = service.createAndDispatch(campaignId);
        auditService.log(AuditAction.WORKFLOW_STARTED, "AgentWorkflow", res.getId(),
                "campaign=" + campaignId, httpReq.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(res, "Agent workflow started"));
    }

    @GetMapping("/campaigns/{campaignId}/agent-workflows")
    public ResponseEntity<ApiResponse<List<AgentWorkflowDto.Response>>> listByCampaign(
            @PathVariable UUID campaignId) {
        return ResponseEntity.ok(ApiResponse.success(service.findByCampaign(campaignId)));
    }

    @GetMapping("/agent-workflows/{id}")
    public ResponseEntity<ApiResponse<AgentWorkflowDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @GetMapping("/agent-workflows/{id}/steps")
    public ResponseEntity<ApiResponse<List<AgentWorkflowDto.StepResultResponse>>> findSteps(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findSteps(id)));
    }

    @PostMapping("/agent-workflows/{id}/retry")
    public ResponseEntity<ApiResponse<AgentWorkflowDto.Response>> retry(
            @PathVariable UUID id,
            Authentication auth,
            HttpServletRequest httpReq) {

        rateLimiter.consume(resolveKey(auth, httpReq));

        AgentWorkflowDto.Response res = service.retry(id);
        auditService.log(AuditAction.WORKFLOW_RETRIED, "AgentWorkflow", id, null, httpReq.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(res, "Workflow retry started"));
    }

    @PostMapping("/agent-workflows/{id}/cancel")
    public ResponseEntity<ApiResponse<AgentWorkflowDto.Response>> cancel(
            @PathVariable UUID id, HttpServletRequest httpReq) {
        AgentWorkflowDto.Response res = service.cancel(id);
        auditService.log(AuditAction.WORKFLOW_CANCELLED, "AgentWorkflow", id, null, httpReq.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(res, "Workflow cancelled"));
    }

    // ── Worker callbacks (authenticated via X-Api-Key → ROLE_SYSTEM) ──────────

    @PostMapping("/agent-workflows/{id}/step-callback")
    public ResponseEntity<Void> stepCallback(
            @PathVariable UUID id,
            @RequestBody AgentWorkflowDto.StepCallbackRequest req) {
        service.handleStepCallback(id, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/agent-workflows/{id}/complete")
    public ResponseEntity<Void> complete(
            @PathVariable UUID id,
            @RequestBody AgentWorkflowDto.CompleteCallbackRequest req) {
        service.handleComplete(id, req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/agent-workflows/{id}/fail")
    public ResponseEntity<Void> fail(
            @PathVariable UUID id,
            @RequestBody AgentWorkflowDto.FailCallbackRequest req) {
        service.handleFail(id, req);
        return ResponseEntity.ok().build();
    }

    private String resolveKey(Authentication auth, HttpServletRequest req) {
        return auth != null ? auth.getName() : req.getRemoteAddr();
    }
}
