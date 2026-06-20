package com.bnpaper.agento.workflow;

import com.bnpaper.agento.brand.BrandProfile;
import com.bnpaper.agento.brand.BrandProfileRepository;
import com.bnpaper.agento.campaign.Campaign;
import com.bnpaper.agento.campaign.CampaignRepository;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.content.ContentStatus;
import com.bnpaper.agento.content.GeneratedContent;
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
import org.springframework.test.util.ReflectionTestUtils;

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
    @Spy  private ObjectMapper objectMapper;

    @InjectMocks
    private AgentWorkflowService service;

    private UUID campaignId;
    private Campaign campaign;
    private BrandProfile brand;

    @BeforeEach
    void setUp() {
        // Inject the API key value that @Value would normally provide
        ReflectionTestUtils.setField(service, "configuredApiKey", "test-key");

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
        when(brandRepo.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(brand));
        when(productRepo.findAll()).thenReturn(List.of());
        when(workerProperties.getCallbackBaseUrl()).thenReturn("http://localhost:8080/api");

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
        when(brandRepo.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(brand));
        when(productRepo.findAll()).thenReturn(List.of());
        when(workerProperties.getCallbackBaseUrl()).thenReturn("http://localhost:8080/api");

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
        when(brandRepo.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());

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
    void findById_populatesGeneratedContentWhenContentExists() throws Exception {
        UUID wfId = UUID.randomUUID();
        String outputPayload = objectMapper.writeValueAsString(
                java.util.Map.of("contentId", UUID.randomUUID().toString(),
                        "complianceWarnings", List.of()));

        AgentWorkflow workflow = AgentWorkflow.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.COMPLETED)
                .outputPayload(outputPayload)
                .build();

        GeneratedContent content = GeneratedContent.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .workflowId(wfId)
                .title("Test Title")
                .body("Test body text")
                .status(ContentStatus.DRAFT)
                .build();

        when(workflowRepo.findById(wfId)).thenReturn(Optional.of(workflow));
        when(stepRepo.findByWorkflowIdOrderByStartedAtAsc(wfId)).thenReturn(List.of());
        when(contentRepo.findFirstByWorkflowIdOrderByCreatedAtDesc(wfId)).thenReturn(Optional.of(content));

        AgentWorkflowDto.Response result = service.findById(wfId);

        assertThat(result.getGeneratedContent()).isNotNull();
        assertThat(result.getGeneratedContent().getTitle()).isEqualTo("Test Title");
        assertThat(result.getGeneratedContent().getStatus()).isEqualTo("DRAFT");
        assertThat(result.getGeneratedContent().getComplianceWarnings()).isEmpty();
    }

    @Test
    void findById_generatedContentIsNullWhenNoContentExists() {
        UUID wfId = UUID.randomUUID();
        AgentWorkflow workflow = AgentWorkflow.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING)
                .build();

        when(workflowRepo.findById(wfId)).thenReturn(Optional.of(workflow));
        when(stepRepo.findByWorkflowIdOrderByStartedAtAsc(wfId)).thenReturn(List.of());
        when(contentRepo.findFirstByWorkflowIdOrderByCreatedAtDesc(wfId)).thenReturn(Optional.empty());

        AgentWorkflowDto.Response result = service.findById(wfId);

        assertThat(result.getGeneratedContent()).isNull();
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
        verify(workerClient).cancelWorkflow(id);
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
    void handleComplete_savesGeneratedContentAsDraft() {
        UUID wfId = UUID.randomUUID();
        AgentWorkflow workflow = AgentWorkflow.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING).build();

        when(workflowRepo.findById(wfId)).thenReturn(Optional.of(workflow));
        when(contentRepo.save(any())).thenAnswer(inv -> {
            GeneratedContent c = inv.getArgument(0);
            // Simulate DB assigning an ID
            GeneratedContent saved = GeneratedContent.builder()
                    .id(UUID.randomUUID())
                    .campaignId(c.getCampaignId())
                    .workflowId(c.getWorkflowId())
                    .contentType(c.getContentType())
                    .title(c.getTitle())
                    .hook(c.getHook())
                    .body(c.getBody())
                    .callToAction(c.getCallToAction())
                    .hashtags(c.getHashtags())
                    .status(c.getStatus())
                    .aiModel(c.getAiModel())
                    .promptVersion(c.getPromptVersion())
                    .complianceNotes(c.getComplianceNotes())
                    .build();
            return saved;
        });
        when(workflowRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentWorkflowDto.CompleteCallbackRequest req = new AgentWorkflowDto.CompleteCallbackRequest();
        req.setFinalContent(new AgentWorkflowDto.CompleteCallbackRequest.FinalContent(
                "Title", "Hook ภาษาไทย", "Body ภาษาไทย", "CTA",
                List.of("#SoClean", "#ทิชชู่SoClean"), "Claim-safe"));
        req.setComplianceWarnings(List.of());

        service.handleComplete(wfId, req);

        verify(contentRepo).save(argThat(c ->
                c.getStatus() == ContentStatus.DRAFT
                        && "AGENT_WORKFLOW".equals(c.getContentType())
                        && "Body ภาษาไทย".equals(c.getBody())
                        && "Hook ภาษาไทย".equals(c.getHook())
                        && "v2".equals(c.getPromptVersion())
                        && wfId.equals(c.getWorkflowId())));

        verify(workflowRepo).save(argThat(w ->
                w.getStatus() == AgentWorkflowStatus.COMPLETED
                        && w.getCurrentStep() == null
                        && w.getOutputPayload() != null));
    }

    @Test
    void handleComplete_setsComplianceWarningWhenTermsDetected() {
        UUID wfId = UUID.randomUUID();
        AgentWorkflow workflow = AgentWorkflow.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING).build();

        when(workflowRepo.findById(wfId)).thenReturn(Optional.of(workflow));
        when(contentRepo.save(any())).thenAnswer(inv -> {
            GeneratedContent c = inv.getArgument(0);
            return GeneratedContent.builder().id(UUID.randomUUID())
                    .campaignId(c.getCampaignId()).workflowId(c.getWorkflowId())
                    .status(c.getStatus()).complianceNotes(c.getComplianceNotes())
                    .hashtags(List.of()).build();
        });
        when(workflowRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentWorkflowDto.CompleteCallbackRequest req = new AgentWorkflowDto.CompleteCallbackRequest();
        req.setFinalContent(new AgentWorkflowDto.CompleteCallbackRequest.FinalContent(
                "Title", "Hook", "Body", "CTA", List.of("#SoClean"), null));
        req.setComplianceWarnings(List.of("dust-free"));

        service.handleComplete(wfId, req);

        verify(contentRepo).save(argThat(c ->
                c.getComplianceNotes() != null
                        && c.getComplianceNotes().contains("WARNING")
                        && c.getComplianceNotes().contains("dust-free")));
    }

    @Test
    void handleStepCallback_ignoresUnknownStatus() {
        UUID wfId = UUID.randomUUID();
        AgentWorkflow workflow = AgentWorkflow.builder()
                .id(wfId).campaignId(campaignId)
                .status(AgentWorkflowStatus.RUNNING).build();

        when(workflowRepo.findById(wfId)).thenReturn(Optional.of(workflow));

        AgentWorkflowDto.StepCallbackRequest req = new AgentWorkflowDto.StepCallbackRequest();
        req.setStepName("brand_strategist");
        req.setStatus("UNKNOWN_STATUS");

        // Should not throw — unknown status is silently ignored
        service.handleStepCallback(wfId, req);

        // Nothing should be persisted for an unknown status
        verify(stepRepo, never()).save(any());
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
