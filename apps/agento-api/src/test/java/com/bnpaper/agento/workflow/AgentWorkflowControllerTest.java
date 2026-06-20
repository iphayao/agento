package com.bnpaper.agento.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgentWorkflowController.class)
class AgentWorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentWorkflowService service;

    @Autowired
    private ObjectMapper objectMapper;

    private AgentWorkflowDto.Response sampleResponse(UUID campaignId) {
        UUID wfId = UUID.randomUUID();
        return AgentWorkflowDto.Response.builder()
                .id(wfId)
                .campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING)
                .currentStep("brand_strategist")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .steps(List.of())
                .build();
    }

    @Test
    void create_returns201() throws Exception {
        UUID campaignId = UUID.randomUUID();
        when(service.createAndDispatch(campaignId)).thenReturn(sampleResponse(campaignId));

        mockMvc.perform(post("/campaigns/{id}/agent-workflows", campaignId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RUNNING"));
    }

    @Test
    void create_returns404WhenCampaignNotFound() throws Exception {
        UUID campaignId = UUID.randomUUID();
        when(service.createAndDispatch(campaignId))
                .thenThrow(new ResourceNotFoundException("Campaign", campaignId));

        mockMvc.perform(post("/campaigns/{id}/agent-workflows", campaignId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void findById_returns200() throws Exception {
        UUID wfId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        when(service.findById(wfId)).thenReturn(sampleResponse(campaignId));

        mockMvc.perform(get("/agent-workflows/{id}", wfId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.currentStep").value("brand_strategist"));
    }

    @Test
    void findById_returns404WhenNotFound() throws Exception {
        UUID wfId = UUID.randomUUID();
        when(service.findById(wfId)).thenThrow(new ResourceNotFoundException("AgentWorkflow", wfId));

        mockMvc.perform(get("/agent-workflows/{id}", wfId))
                .andExpect(status().isNotFound());
    }

    @Test
    void findSteps_returns200WithStepList() throws Exception {
        UUID wfId = UUID.randomUUID();
        AgentWorkflowDto.StepResultResponse step = AgentWorkflowDto.StepResultResponse.builder()
                .id(UUID.randomUUID())
                .workflowId(wfId)
                .stepName("brand_strategist")
                .status(AgentStepStatus.COMPLETED)
                .outputPayload("{\"brief\":\"strategy\"}")
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(service.findSteps(wfId)).thenReturn(List.of(step));

        mockMvc.perform(get("/agent-workflows/{id}/steps", wfId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].stepName").value("brand_strategist"))
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"));
    }

    @Test
    void retry_returns200() throws Exception {
        UUID wfId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        AgentWorkflowDto.Response retried = AgentWorkflowDto.Response.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING)
                .currentStep("brand_strategist")
                .steps(List.of())
                .build();

        when(service.retry(wfId)).thenReturn(retried);

        mockMvc.perform(post("/agent-workflows/{id}/retry", wfId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void retry_returns400WhenStillRunning() throws Exception {
        UUID wfId = UUID.randomUUID();
        when(service.retry(wfId)).thenThrow(new IllegalStateException("Workflow is still running."));

        mockMvc.perform(post("/agent-workflows/{id}/retry", wfId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void cancel_returns200() throws Exception {
        UUID wfId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        AgentWorkflowDto.Response cancelled = AgentWorkflowDto.Response.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.CANCELLED)
                .steps(List.of())
                .build();

        when(service.cancel(wfId)).thenReturn(cancelled);

        mockMvc.perform(post("/agent-workflows/{id}/cancel", wfId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void stepCallback_returns200() throws Exception {
        UUID wfId = UUID.randomUUID();
        doNothing().when(service).handleStepCallback(eq(wfId), any());

        AgentWorkflowDto.StepCallbackRequest req = new AgentWorkflowDto.StepCallbackRequest();
        req.setStepName("brand_strategist");
        req.setStatus("COMPLETED");

        mockMvc.perform(post("/agent-workflows/{id}/step-callback", wfId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void completeCallback_returns200() throws Exception {
        UUID wfId = UUID.randomUUID();
        doNothing().when(service).handleComplete(eq(wfId), any());

        AgentWorkflowDto.CompleteCallbackRequest req = new AgentWorkflowDto.CompleteCallbackRequest();
        req.setFinalContent(new AgentWorkflowDto.CompleteCallbackRequest.FinalContent(
                "Title", "Hook", "Body", "CTA", List.of("#SoClean"), "Claim-safe"));
        req.setComplianceWarnings(List.of());

        mockMvc.perform(post("/agent-workflows/{id}/complete", wfId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void failCallback_returns200() throws Exception {
        UUID wfId = UUID.randomUUID();
        doNothing().when(service).handleFail(eq(wfId), any());

        AgentWorkflowDto.FailCallbackRequest req = new AgentWorkflowDto.FailCallbackRequest(
                "LLM timeout", "content_writer");

        mockMvc.perform(post("/agent-workflows/{id}/fail", wfId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
