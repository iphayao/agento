package com.bnpaper.agento.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AgentWorkflowDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID campaignId;
        private AgentWorkflowStatus status;
        private String currentStep;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<StepResultResponse> steps;
        private GeneratedContentSummary generatedContent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepResultResponse {
        private UUID id;
        private UUID workflowId;
        private String stepName;
        private AgentStepStatus status;
        private String outputPayload;
        private String errorMessage;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedContentSummary {
        private UUID contentId;
        private String title;
        private String status;
        private List<String> complianceWarnings;
    }

    // Called by Python worker to update a single step
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepCallbackRequest {
        private String stepName;
        private String status;   // RUNNING | COMPLETED | FAILED
        private String inputPayload;
        private String outputPayload;
        private String errorMessage;
    }

    // Called by Python worker when all steps are done
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompleteCallbackRequest {
        private FinalContent finalContent;
        private List<String> complianceWarnings;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class FinalContent {
            private String title;
            private String hook;
            private String body;
            private String callToAction;
            private List<String> hashtags;
            private String complianceNotes;
        }
    }

    // Called by Python worker on hard failure
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FailCallbackRequest {
        private String errorMessage;
        private String failedStep;
    }

    // Outgoing dispatch request to Python worker
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DispatchRequest {
        private String workflowId;
        private String campaignId;
        private String callbackBaseUrl;
        private String callbackApiKey;
        private Object brand;
        private List<Object> products;
        private Object campaign;
    }

    static Response toResponse(AgentWorkflow w, List<AgentStepResult> steps) {
        return Response.builder()
                .id(w.getId())
                .campaignId(w.getCampaignId())
                .status(w.getStatus())
                .currentStep(w.getCurrentStep())
                .errorMessage(w.getErrorMessage())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .steps(steps.stream().map(AgentWorkflowDto::toStepResponse).toList())
                .build();
    }

    static StepResultResponse toStepResponse(AgentStepResult s) {
        return StepResultResponse.builder()
                .id(s.getId())
                .workflowId(s.getWorkflowId())
                .stepName(s.getStepName())
                .status(s.getStatus())
                .outputPayload(s.getOutputPayload())
                .errorMessage(s.getErrorMessage())
                .startedAt(s.getStartedAt())
                .completedAt(s.getCompletedAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
