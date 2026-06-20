package com.bnpaper.agento.workflow;

import com.bnpaper.agento.brand.BrandProfile;
import com.bnpaper.agento.brand.BrandProfileRepository;
import com.bnpaper.agento.campaign.Campaign;
import com.bnpaper.agento.campaign.CampaignRepository;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.common.security.ApiKeyFilter;
import com.bnpaper.agento.content.GeneratedContentRepository;
import com.bnpaper.agento.product.ProductFactRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentWorkflowServiceTest {

    @Mock private AgentWorkflowRepository workflowRepo;
    @Mock private AgentStepResultRepository stepRepo;
    @Mock private CampaignRepository campaignRepo;
    @Mock private BrandProfileRepository brandRepo;
    @Mock private ProductFactRepository productRepo;
    @Mock private GeneratedContentRepository contentRepo;
    @Mock private WorkerClient workerClient;
    @Mock private WorkerProperties workerProperties;
    @Mock private ApiKeyFilter apiKeyFilter;
    @Spy  private ObjectMapper objectMapper;

    @InjectMocks
    private AgentWorkflowService service;

    private UUID campaignId;
    private Campaign campaign;
    private BrandProfile brand;

    @BeforeEach
    void setUp() {
        campaignId = UUID.randomUUID();
        campaign = Campaign.builder()
                .id(campaignId)
                .name("TikTok June")
                .objective("Drive TikTok sales")
                .channel("tiktok")
                .targetAudience("Women Gen Y")
                .contentAngle("ฝุ่นน้อย เหมาะสำหรับออฟฟิศ")
                .status("ACTIVE")
                .build();
        brand = BrandProfile.builder()
                .id(UUID.randomUUID())
                .brandName("SoClean")
                .slogan("สะอาด เนียนนุ่ม ฝุ่นน้อย")
                .keyMessages(List.of("ฝุ่นน้อย"))
                .prohibitedClaims(List.of())
                .build();
    }

    @Test
    void createAndDispatch_successfulDispatch_returnsRunningWorkflow() {
        when(campaignRepo.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(brandRepo.findAll()).thenReturn(List.of(brand));
        when(productRepo.findAll()).thenReturn(List.of());
        when(workerProperties.getCallbackBaseUrl()).thenReturn("http://localhost:8080/api");
        when(apiKeyFilter.getConfiguredKey()).thenReturn("test-key");

        AgentWorkflow saved = AgentWorkflow.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING)
                .currentStep("brand_strategist")
                .build();

        when(workflowRepo.save(any())).thenReturn(saved);
        when(stepRepo.findByWorkflowIdOrderByStartedAtAsc(any())).thenReturn(List.of());
        doNothing().when(workerClient).dispatch(any());

        AgentWorkflowDto.Response result = service.createAndDispatch(campaignId);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AgentWorkflowStatus.RUNNING);
        verify(workerClient).dispatch(any());
    }

    @Test
    void createAndDispatch_workerUnavailable_returnsFailedWorkflow() {
        when(campaignRepo.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(brandRepo.findAll()).thenReturn(List.of(brand));
        when(productRepo.findAll()).thenReturn(List.of());
        when(workerProperties.getCallbackBaseUrl()).thenReturn("http://localhost:8080/api");
        when(apiKeyFilter.getConfiguredKey()).thenReturn("");

        AgentWorkflow failedWorkflow = AgentWorkflow.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .status(AgentWorkflowStatus.FAILED)
                .errorMessage("agento-worker unavailable: connection refused")
                .build();

        doThrow(new WorkerClient.WorkerUnavailableException("connection refused", new RuntimeException()))
                .when(workerClient).dispatch(any());
        when(workflowRepo.save(any())).thenReturn(failedWorkflow);
        when(stepRepo.findByWorkflowIdOrderByStartedAtAsc(any())).thenReturn(List.of());

        AgentWorkflowDto.Response result = service.createAndDispatch(campaignId);

        assertThat(result.getStatus()).isEqualTo(AgentWorkflowStatus.FAILED);
        assertThat(result.getErrorMessage()).contains("unavailable");
    }

    @Test
    void createAndDispatch_throwsWhenCampaignNotFound() {
        when(campaignRepo.findById(campaignId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAndDispatch(campaignId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAndDispatch_throwsWhenNoBrandProfile() {
        when(campaignRepo.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(brandRepo.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.createAndDispatch(campaignId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("brand profile");
    }

    @Test
    void findById_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(workflowRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancel_setsStatusCancelled() {
        UUID id = UUID.randomUUID();
        AgentWorkflow running = AgentWorkflow.builder()
                .id(id)
                .campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING)
                .build();
        AgentWorkflow cancelled = AgentWorkflow.builder()
                .id(id)
                .campaignId(campaignId)
                .status(AgentWorkflowStatus.CANCELLED)
                .build();

        when(workflowRepo.findById(id)).thenReturn(Optional.of(running));
        when(workflowRepo.save(any())).thenReturn(cancelled);
        when(stepRepo.findByWorkflowIdOrderByStartedAtAsc(id)).thenReturn(List.of());

        AgentWorkflowDto.Response result = service.cancel(id);

        assertThat(result.getStatus()).isEqualTo(AgentWorkflowStatus.CANCELLED);
    }

    @Test
    void cancel_throwsWhenAlreadyCompleted() {
        UUID id = UUID.randomUUID();
        AgentWorkflow completed = AgentWorkflow.builder()
                .id(id)
                .campaignId(campaignId)
                .status(AgentWorkflowStatus.COMPLETED)
                .build();

        when(workflowRepo.findById(id)).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> service.cancel(id))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void retry_throwsWhenStillRunning() {
        UUID id = UUID.randomUUID();
        AgentWorkflow running = AgentWorkflow.builder()
                .id(id).campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING).build();

        when(workflowRepo.findById(id)).thenReturn(Optional.of(running));

        assertThatThrownBy(() -> service.retry(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("running");
    }

    @Test
    void handleStepCallback_upsertsPendingStep() {
        UUID wfId = UUID.randomUUID();
        AgentWorkflow workflow = AgentWorkflow.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING).build();

        when(workflowRepo.findById(wfId)).thenReturn(Optional.of(workflow));
        when(stepRepo.findByWorkflowIdOrderByStartedAtAsc(wfId)).thenReturn(List.of());
        when(stepRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentWorkflowDto.StepCallbackRequest req = new AgentWorkflowDto.StepCallbackRequest();
        req.setStepName("brand_strategist");
        req.setStatus("RUNNING");
        req.setOutputPayload("{\"brief\":\"strategy\"}");

        service.handleStepCallback(wfId, req);

        verify(stepRepo).save(argThat(step ->
                step.getStepName().equals("brand_strategist")
                        && step.getStatus() == AgentStepStatus.RUNNING));
    }

    @Test
    void handleFail_setsWorkflowFailed() {
        UUID wfId = UUID.randomUUID();
        AgentWorkflow workflow = AgentWorkflow.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING).build();

        when(workflowRepo.findById(wfId)).thenReturn(Optional.of(workflow));
        when(workflowRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentWorkflowDto.FailCallbackRequest req = new AgentWorkflowDto.FailCallbackRequest(
                "LLM timeout", "content_writer");
        service.handleFail(wfId, req);

        verify(workflowRepo).save(argThat(w ->
                w.getStatus() == AgentWorkflowStatus.FAILED
                        && "LLM timeout".equals(w.getErrorMessage())));
    }
}
