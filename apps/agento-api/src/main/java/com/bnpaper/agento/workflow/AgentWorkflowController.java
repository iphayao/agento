package com.bnpaper.agento.workflow;

import com.bnpaper.agento.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AgentWorkflowController {

    private final AgentWorkflowService service;

    @PostMapping("/campaigns/{campaignId}/agent-workflows")
    public ResponseEntity<ApiResponse<AgentWorkflowDto.Response>> create(
            @PathVariable UUID campaignId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createAndDispatch(campaignId), "Agent workflow started"));
    }

    @GetMapping("/campaigns/{campaignId}/agent-workflows")
    public ResponseEntity<ApiResponse<List<AgentWorkflowDto.Response>>> listByCampaign(
            @PathVariable UUID campaignId) {
        return ResponseEntity.ok(ApiResponse.success(service.findByCampaign(campaignId)));
    }

    @GetMapping("/agent-workflows/{id}")
    public ResponseEntity<ApiResponse<AgentWorkflowDto.Response>> findById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @GetMapping("/agent-workflows/{id}/steps")
    public ResponseEntity<ApiResponse<List<AgentWorkflowDto.StepResultResponse>>> findSteps(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findSteps(id)));
    }

    @PostMapping("/agent-workflows/{id}/retry")
    public ResponseEntity<ApiResponse<AgentWorkflowDto.Response>> retry(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.retry(id), "Workflow retry started"));
    }

    @PostMapping("/agent-workflows/{id}/cancel")
    public ResponseEntity<ApiResponse<AgentWorkflowDto.Response>> cancel(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.cancel(id), "Workflow cancelled"));
    }

    // Internal callbacks from agento-worker — not exposed to frontend directly

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
}
