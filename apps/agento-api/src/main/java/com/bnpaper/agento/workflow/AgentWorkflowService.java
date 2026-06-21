package com.bnpaper.agento.workflow;

import com.bnpaper.agento.brand.BrandProfile;
import com.bnpaper.agento.brand.BrandProfileRepository;
import com.bnpaper.agento.calendar.BatchGenerationJob;
import com.bnpaper.agento.calendar.BatchGenerationJobRepository;
import com.bnpaper.agento.calendar.BatchJobStatus;
import com.bnpaper.agento.calendar.CalendarItem;
import com.bnpaper.agento.calendar.CalendarItemRepository;
import com.bnpaper.agento.calendar.CalendarItemStatus;
import com.bnpaper.agento.calendar.CalendarStatus;
import com.bnpaper.agento.calendar.ContentCalendar;
import com.bnpaper.agento.calendar.ContentCalendarRepository;
import com.bnpaper.agento.campaign.Campaign;
import com.bnpaper.agento.campaign.CampaignRepository;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.content.ContentStatus;
import com.bnpaper.agento.content.GeneratedContent;
import com.bnpaper.agento.content.GeneratedContentRepository;
import com.bnpaper.agento.product.ProductFact;
import com.bnpaper.agento.product.ProductFactRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentWorkflowService {

    private final AgentWorkflowRepository workflowRepo;
    private final AgentStepResultRepository stepRepo;
    private final CampaignRepository campaignRepo;
    private final BrandProfileRepository brandRepo;
    private final ProductFactRepository productRepo;
    private final GeneratedContentRepository contentRepo;
    private final CalendarItemRepository calendarItemRepo;
    private final BatchGenerationJobRepository batchJobRepo;
    private final ContentCalendarRepository calendarRepo;
    private final WorkerClient workerClient;
    private final WorkerProperties workerProperties;
    private final ObjectMapper objectMapper;

    @Value("${agento.security.api-key:}")
    private String configuredApiKey;

    /** Creates a workflow record and dispatches it to agento-worker. */
    @Transactional
    public AgentWorkflowDto.Response createAndDispatch(UUID campaignId) {
        Campaign campaign = campaignRepo.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", campaignId));

        BrandProfile brand = brandRepo.findFirstByOrderByCreatedAtAsc()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No brand profile found. Please create a brand profile before running a workflow."));

        List<ProductFact> products = productRepo.findAll();

        AgentWorkflow workflow = AgentWorkflow.builder()
                .campaignId(campaignId)
                .status(AgentWorkflowStatus.PENDING)
                .build();

        String inputJson = buildInputJson(campaign, brand, products);
        workflow.setInputPayload(inputJson);
        workflow = workflowRepo.save(workflow);

        AgentWorkflowDto.DispatchRequest dispatch = AgentWorkflowDto.DispatchRequest.builder()
                .workflowId(workflow.getId().toString())
                .campaignId(campaignId.toString())
                .callbackBaseUrl(workerProperties.getCallbackBaseUrl())
                .callbackApiKey(configuredApiKey)
                .brand(brandToMap(brand))
                .products(productsToList(products))
                .campaign(campaignToMap(campaign))
                .build();

        try {
            workerClient.dispatch(dispatch);
            workflow.setStatus(AgentWorkflowStatus.RUNNING);
            workflow.setCurrentStep("brand_strategist");
        } catch (WorkerClient.WorkerUnavailableException e) {
            log.warn("Worker unavailable — workflow {} stays PENDING: {}", workflow.getId(), e.getMessage());
            workflow.setStatus(AgentWorkflowStatus.FAILED);
            workflow.setErrorMessage("agento-worker unavailable: " + e.getMessage());
        }

        workflow = workflowRepo.save(workflow);
        List<AgentStepResult> steps = stepRepo.findByWorkflowIdOrderByStartedAtAsc(workflow.getId());
        return AgentWorkflowDto.toResponse(workflow, steps);
    }

    /** Creates a workflow for a single calendar item and dispatches it to agento-worker. */
    @Transactional
    public AgentWorkflow createAndDispatchForCalendarItem(UUID calendarItemId) {
        CalendarItem item = calendarItemRepo.findById(calendarItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarItem", calendarItemId));

        BrandProfile brand = brandRepo.findFirstByOrderByCreatedAtAsc()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No brand profile found. Please create a brand profile before generating content."));

        List<ProductFact> products = productRepo.findAll();

        Map<String, Object> campaignContext = Map.of(
                "id", calendarItemId.toString(),
                "name", "Calendar " + item.getPlannedDate() + " — " + item.getChannel(),
                "objective", resolveCalendarObjective(item.getCalendarId()),
                "channel", item.getChannel(),
                "targetAudience", nullToEmpty(item.getTargetAudience()),
                "contentAngle", nullToEmpty(item.getContentAngle())
        );

        String inputJson = buildInputJsonForCalendarItem(campaignContext, brand, products, item);

        AgentWorkflow workflow = AgentWorkflow.builder()
                .calendarItemId(calendarItemId)
                .status(AgentWorkflowStatus.PENDING)
                .inputPayload(inputJson)
                .build();
        workflow = workflowRepo.save(workflow);

        AgentWorkflowDto.DispatchRequest dispatch = AgentWorkflowDto.DispatchRequest.builder()
                .workflowId(workflow.getId().toString())
                .campaignId(calendarItemId.toString())
                .callbackBaseUrl(workerProperties.getCallbackBaseUrl())
                .callbackApiKey(configuredApiKey)
                .brand(brandToMap(brand))
                .products(productsToList(products))
                .campaign(campaignContext)
                .build();

        try {
            workerClient.dispatch(dispatch);
            workflow.setStatus(AgentWorkflowStatus.RUNNING);
            workflow.setCurrentStep("brand_strategist");
        } catch (WorkerClient.WorkerUnavailableException e) {
            log.warn("Worker unavailable — calendar item workflow {} stays PENDING: {}",
                    workflow.getId(), e.getMessage());
            workflow.setStatus(AgentWorkflowStatus.FAILED);
            workflow.setErrorMessage("agento-worker unavailable: " + e.getMessage());
        }

        workflow = workflowRepo.save(workflow);

        item.setWorkflowId(workflow.getId());
        if (workflow.getStatus() == AgentWorkflowStatus.FAILED) {
            item.setStatus(CalendarItemStatus.FAILED);
        }
        calendarItemRepo.save(item);

        return workflow;
    }

    public List<AgentWorkflowDto.Response> findByCampaign(UUID campaignId) {
        return workflowRepo.findByCampaignIdOrderByCreatedAtDesc(campaignId).stream()
                .map(w -> {
                    List<AgentStepResult> steps = stepRepo.findByWorkflowIdOrderByStartedAtAsc(w.getId());
                    AgentWorkflowDto.Response response = AgentWorkflowDto.toResponse(w, steps);
                    populateGeneratedContent(response, w);
                    return response;
                })
                .toList();
    }

    public AgentWorkflowDto.Response findById(UUID id) {
        AgentWorkflow workflow = workflowRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AgentWorkflow", id));
        List<AgentStepResult> steps = stepRepo.findByWorkflowIdOrderByStartedAtAsc(id);
        AgentWorkflowDto.Response response = AgentWorkflowDto.toResponse(workflow, steps);
        populateGeneratedContent(response, workflow);
        return response;
    }

    public List<AgentWorkflowDto.StepResultResponse> findSteps(UUID workflowId) {
        if (!workflowRepo.existsById(workflowId)) {
            throw new ResourceNotFoundException("AgentWorkflow", workflowId);
        }
        return stepRepo.findByWorkflowIdOrderByStartedAtAsc(workflowId).stream()
                .map(AgentWorkflowDto::toStepResponse)
                .toList();
    }

    @Transactional
    public AgentWorkflowDto.Response retry(UUID id) {
        AgentWorkflow workflow = workflowRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AgentWorkflow", id));

        if (workflow.getStatus() == AgentWorkflowStatus.RUNNING) {
            throw new IllegalStateException("Workflow is still running. Cancel it first.");
        }
        if (workflow.getStatus() == AgentWorkflowStatus.COMPLETED) {
            throw new IllegalStateException("Workflow is already completed.");
        }

        stepRepo.deleteByWorkflowId(id);
        workflow.setStatus(AgentWorkflowStatus.PENDING);
        workflow.setCurrentStep(null);
        workflow.setErrorMessage(null);
        workflow.setOutputPayload(null);
        workflow = workflowRepo.save(workflow);

        AgentWorkflowDto.DispatchRequest dispatch = buildDispatchFromStoredPayload(workflow);
        try {
            workerClient.dispatch(dispatch);
            workflow.setStatus(AgentWorkflowStatus.RUNNING);
            workflow.setCurrentStep("brand_strategist");
        } catch (WorkerClient.WorkerUnavailableException e) {
            workflow.setStatus(AgentWorkflowStatus.FAILED);
            workflow.setErrorMessage("agento-worker unavailable: " + e.getMessage());
        }

        workflow = workflowRepo.save(workflow);
        return AgentWorkflowDto.toResponse(workflow, List.of());
    }

    @Transactional
    public AgentWorkflowDto.Response cancel(UUID id) {
        AgentWorkflow workflow = workflowRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AgentWorkflow", id));

        if (workflow.getStatus() == AgentWorkflowStatus.COMPLETED
                || workflow.getStatus() == AgentWorkflowStatus.CANCELLED) {
            throw new IllegalStateException("Workflow cannot be cancelled in status: " + workflow.getStatus());
        }

        workflow.setStatus(AgentWorkflowStatus.CANCELLED);
        workflow = workflowRepo.save(workflow);

        // Best-effort: signal Python worker to stop executing remaining steps
        workerClient.cancelWorkflow(id);

        List<AgentStepResult> steps = stepRepo.findByWorkflowIdOrderByStartedAtAsc(id);
        return AgentWorkflowDto.toResponse(workflow, steps);
    }

    // --- Callbacks from Python worker ---

    @Transactional
    public void handleStepCallback(UUID workflowId, AgentWorkflowDto.StepCallbackRequest req) {
        AgentWorkflow workflow = workflowRepo.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentWorkflow", workflowId));

        if (workflow.getStatus() == AgentWorkflowStatus.CANCELLED) {
            log.info("Ignoring step callback for cancelled workflow {}", workflowId);
            return;
        }

        AgentStepStatus stepStatus;
        try {
            stepStatus = AgentStepStatus.valueOf(req.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Ignoring step callback with unknown status '{}' for workflow {}", req.getStatus(), workflowId);
            return;
        }

        // Upsert: find existing step record or create new
        List<AgentStepResult> existing = stepRepo.findByWorkflowIdOrderByStartedAtAsc(workflowId);
        AgentStepResult step = existing.stream()
                .filter(s -> s.getStepName().equals(req.getStepName()))
                .findFirst()
                .orElse(AgentStepResult.builder()
                        .workflowId(workflowId)
                        .stepName(req.getStepName())
                        .build());

        step.setStatus(stepStatus);
        step.setInputPayload(req.getInputPayload());
        step.setOutputPayload(req.getOutputPayload());
        step.setErrorMessage(req.getErrorMessage());

        if (stepStatus == AgentStepStatus.RUNNING && step.getStartedAt() == null) {
            step.setStartedAt(LocalDateTime.now());
        }
        if (stepStatus == AgentStepStatus.COMPLETED || stepStatus == AgentStepStatus.FAILED) {
            step.setCompletedAt(LocalDateTime.now());
        }

        stepRepo.save(step);

        if (stepStatus == AgentStepStatus.RUNNING) {
            workflow.setCurrentStep(req.getStepName());
            workflowRepo.save(workflow);
        }
    }

    @Transactional
    public void handleComplete(UUID workflowId, AgentWorkflowDto.CompleteCallbackRequest req) {
        AgentWorkflow workflow = workflowRepo.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentWorkflow", workflowId));

        if (workflow.getStatus() == AgentWorkflowStatus.CANCELLED) {
            log.info("Ignoring complete callback for cancelled workflow {}", workflowId);
            return;
        }

        AgentWorkflowDto.CompleteCallbackRequest.FinalContent fc = req.getFinalContent();
        List<String> warnings = req.getComplianceWarnings() != null ? req.getComplianceWarnings() : List.of();

        String complianceNotes = warnings.isEmpty()
                ? "Claim-safe — no prohibited terms detected"
                : "WARNING — prohibited terms detected: " + String.join(", ", warnings);

        GeneratedContent content = GeneratedContent.builder()
                .campaignId(workflow.getCampaignId())
                .calendarItemId(workflow.getCalendarItemId())
                .workflowId(workflowId)
                .contentType("AGENT_WORKFLOW")
                .title(fc.getTitle())
                .hook(fc.getHook())
                .body(fc.getBody())
                .callToAction(fc.getCallToAction())
                .hashtags(fc.getHashtags() != null ? fc.getHashtags() : List.of())
                .status(ContentStatus.DRAFT)
                .aiModel("langgraph-7-step")
                .promptVersion("v2")
                .complianceNotes(fc.getComplianceNotes() != null ? fc.getComplianceNotes() : complianceNotes)
                .build();

        GeneratedContent saved = contentRepo.save(content);

        try {
            Map<String, Object> output = Map.of(
                    "contentId", saved.getId().toString(),
                    "complianceWarnings", warnings
            );
            workflow.setOutputPayload(objectMapper.writeValueAsString(output));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize output payload: {}", e.getMessage());
        }

        workflow.setStatus(AgentWorkflowStatus.COMPLETED);
        workflow.setCurrentStep(null);
        workflow.setErrorMessage(null);
        workflowRepo.save(workflow);

        if (workflow.getCalendarItemId() != null) {
            markCalendarItemCompleted(workflow.getCalendarItemId(), saved.getId());
        }

        log.info("Workflow {} completed — GeneratedContent {} saved as DRAFT", workflowId, saved.getId());
    }

    @Transactional
    public void handleFail(UUID workflowId, AgentWorkflowDto.FailCallbackRequest req) {
        AgentWorkflow workflow = workflowRepo.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("AgentWorkflow", workflowId));

        if (workflow.getStatus() == AgentWorkflowStatus.CANCELLED) {
            return;
        }

        workflow.setStatus(AgentWorkflowStatus.FAILED);
        workflow.setErrorMessage(req.getErrorMessage());
        if (req.getFailedStep() != null) {
            workflow.setCurrentStep(req.getFailedStep());
        }
        workflowRepo.save(workflow);

        if (workflow.getCalendarItemId() != null) {
            markCalendarItemFailed(workflow.getCalendarItemId(), req.getErrorMessage());
        }

        log.warn("Workflow {} failed at step {}: {}", workflowId, req.getFailedStep(), req.getErrorMessage());
    }

    // --- Helpers ---

    /** Populate the generatedContent summary on a response if this workflow produced content. */
    private void populateGeneratedContent(AgentWorkflowDto.Response response, AgentWorkflow workflow) {
        contentRepo.findFirstByWorkflowIdOrderByCreatedAtDesc(workflow.getId()).ifPresent(content -> {
            List<String> warnings = parseComplianceWarnings(workflow.getOutputPayload());
            response.setGeneratedContent(new AgentWorkflowDto.GeneratedContentSummary(
                    content.getId(),
                    content.getTitle(),
                    content.getStatus().name(),
                    warnings
            ));
        });
    }

    /** Extract complianceWarnings list from the workflow's JSON output payload. */
    private List<String> parseComplianceWarnings(String outputPayload) {
        if (outputPayload == null || outputPayload.isBlank()) return List.of();
        try {
            Map<String, Object> map = objectMapper.readValue(outputPayload, new TypeReference<>() {});
            Object warnings = map.get("complianceWarnings");
            if (warnings instanceof List<?> list) {
                return list.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .toList();
            }
        } catch (JsonProcessingException e) {
            log.debug("Could not parse compliance warnings from output payload: {}", e.getMessage());
        }
        return List.of();
    }

    private String buildInputJson(Campaign c, BrandProfile b, List<ProductFact> products) {
        try {
            Map<String, Object> payload = Map.of(
                    "campaign", campaignToMap(c),
                    "brand", brandToMap(b),
                    "products", productsToList(products)
            );
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize workflow input payload", e);
        }
    }

    /** Rebuild a dispatch request from the stored input payload for workflow retry. */
    private AgentWorkflowDto.DispatchRequest buildDispatchFromStoredPayload(AgentWorkflow workflow) {
        try {
            StoredInputPayload payload = objectMapper.readValue(
                    workflow.getInputPayload(), StoredInputPayload.class);
            return AgentWorkflowDto.DispatchRequest.builder()
                    .workflowId(workflow.getId().toString())
                    .campaignId(workflow.getCampaignId().toString())
                    .callbackBaseUrl(workerProperties.getCallbackBaseUrl())
                    .callbackApiKey(configuredApiKey)
                    .brand(payload.getBrand() != null ? payload.getBrand() : Map.of())
                    .products(payload.getProducts() != null ? payload.getProducts() : List.of())
                    .campaign(payload.getCampaign() != null ? payload.getCampaign() : Map.of())
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse stored input payload for retry", e);
        }
    }

    private Map<String, Object> campaignToMap(Campaign c) {
        return Map.of(
                "id", c.getId().toString(),
                "name", c.getName(),
                "objective", nullToEmpty(c.getObjective()),
                "channel", nullToEmpty(c.getChannel()),
                "targetAudience", nullToEmpty(c.getTargetAudience()),
                "contentAngle", nullToEmpty(c.getContentAngle())
        );
    }

    private Map<String, Object> brandToMap(BrandProfile b) {
        return Map.of(
                "brandName", b.getBrandName(),
                "slogan", nullToEmpty(b.getSlogan()),
                "toneOfVoice", nullToEmpty(b.getToneOfVoice()),
                "targetAudience", nullToEmpty(b.getTargetAudience()),
                "keyMessages", b.getKeyMessages() != null ? b.getKeyMessages() : List.of(),
                "prohibitedClaims", b.getProhibitedClaims() != null ? b.getProhibitedClaims() : List.of()
        );
    }

    private List<Object> productsToList(List<ProductFact> products) {
        List<Object> result = new ArrayList<>();
        for (ProductFact p : products) {
            result.add(Map.of(
                    "productName", p.getProductName(),
                    "sku", nullToEmpty(p.getSku()),
                    "ply", p.getPly(),
                    "sheetCount", p.getSheetCount(),
                    "packSize", p.getPackSize(),
                    "cartonSize", p.getCartonSize(),
                    "keyBenefits", p.getKeyBenefits() != null ? p.getKeyBenefits() : List.of(),
                    "proofPoints", p.getProofPoints() != null ? p.getProofPoints() : List.of()
            ));
        }
        return result;
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private String resolveCalendarObjective(UUID calendarId) {
        return calendarRepo.findById(calendarId)
                .map(ContentCalendar::getObjective)
                .map(this::nullToEmpty)
                .orElse("");
    }

    private String buildInputJsonForCalendarItem(
            Map<String, Object> campaignContext, BrandProfile brand,
            List<ProductFact> products, CalendarItem item) {
        try {
            Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("campaign", campaignContext);
            payload.put("brand", brandToMap(brand));
            payload.put("products", productsToList(products));
            payload.put("calendarItem", Map.of(
                    "hookDirection", nullToEmpty(item.getHookDirection()),
                    "ctaDirection", nullToEmpty(item.getCtaDirection()),
                    "targetAudience", nullToEmpty(item.getTargetAudience()),
                    "contentAngle", nullToEmpty(item.getContentAngle()),
                    "contentType", nullToEmpty(item.getContentType())
            ));
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize calendar item workflow input", e);
        }
    }

    @Transactional
    protected void markCalendarItemCompleted(UUID calendarItemId, UUID contentId) {
        CalendarItem item = calendarItemRepo.findById(calendarItemId).orElse(null);
        if (item == null) return;
        item.setGeneratedContentId(contentId);
        item.setStatus(CalendarItemStatus.COMPLETED);
        calendarItemRepo.save(item);
        updateBatchJobProgress(item.getCalendarId());
    }

    @Transactional
    protected void markCalendarItemFailed(UUID calendarItemId, String errorMessage) {
        CalendarItem item = calendarItemRepo.findById(calendarItemId).orElse(null);
        if (item == null) return;
        item.setStatus(CalendarItemStatus.FAILED);
        calendarItemRepo.save(item);
        updateBatchJobProgress(item.getCalendarId());
    }

    private void updateBatchJobProgress(UUID calendarId) {
        batchJobRepo.findFirstByCalendarIdOrderByCreatedAtDesc(calendarId).ifPresent(job -> {
            if (job.getStatus() == BatchJobStatus.RUNNING) {
                List<CalendarItem> items = calendarItemRepo
                        .findByCalendarIdOrderByPlannedDateAsc(calendarId);
                long completed = items.stream()
                        .filter(i -> i.getStatus() == CalendarItemStatus.COMPLETED).count();
                long failed = items.stream()
                        .filter(i -> i.getStatus() == CalendarItemStatus.FAILED).count();
                job.setCompletedItems((int) completed);
                job.setFailedItems((int) failed);

                if (completed + failed >= job.getTotalItems()) {
                    job.setStatus(BatchJobStatus.COMPLETED);
                    calendarRepo.findById(calendarId).ifPresent(cal -> {
                        cal.setStatus(CalendarStatus.READY_FOR_REVIEW);
                        calendarRepo.save(cal);
                    });
                }
                batchJobRepo.save(job);
            }
        });
    }

    /** Typed container for deserializing the stored workflow input payload on retry. */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class StoredInputPayload {
        private Map<String, Object> brand;
        private List<Object> products;
        private Map<String, Object> campaign;
    }
}
