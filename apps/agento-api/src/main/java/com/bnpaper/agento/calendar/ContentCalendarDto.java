package com.bnpaper.agento.calendar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ContentCalendarDto {

    @Data
    @NoArgsConstructor
    public static class CreateRequest {
        @NotBlank
        private String name;
        @NotNull
        private LocalDate periodStart;
        @NotNull
        private LocalDate periodEnd;
        private String objective;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateRequest {
        private String name;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private String objective;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarResponse {
        private UUID id;
        private String name;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private String objective;
        private String status;
        private int itemCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    public static class ItemCreateRequest {
        @NotNull
        private LocalDate plannedDate;
        @NotBlank
        private String channel;
        private String contentType;
        private String contentAngle;
        private String targetAudience;
        private String hookDirection;
        private String ctaDirection;
    }

    @Data
    @NoArgsConstructor
    public static class ItemUpdateRequest {
        private LocalDate plannedDate;
        private String channel;
        private String contentType;
        private String contentAngle;
        private String targetAudience;
        private String hookDirection;
        private String ctaDirection;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private UUID id;
        private UUID calendarId;
        private LocalDate plannedDate;
        private String channel;
        private String contentType;
        private String contentAngle;
        private String targetAudience;
        private String hookDirection;
        private String ctaDirection;
        private String status;
        private UUID generatedContentId;
        private UUID workflowId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchJobResponse {
        private UUID id;
        private UUID calendarId;
        private String status;
        private int totalItems;
        private int completedItems;
        private int failedItems;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ── Worker request/response for calendar planning ──────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanWorkerRequest {
        private String calendarId;
        private Map<String, Object> brand;
        private List<Object> products;
        private String periodStart;
        private String periodEnd;
        private String objective;
        private int numItems;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanWorkerResponse {
        private String calendarId;
        private List<ItemSuggestion> suggestions;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemSuggestion {
        private String plannedDate;
        private String channel;
        private String contentType;
        private String contentAngle;
        private String targetAudience;
        private String hookDirection;
        private String ctaDirection;
    }

    // ── Mapping helpers ────────────────────────────────────────────────────────

    static CalendarResponse toCalendarResponse(ContentCalendar c, int itemCount) {
        return CalendarResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .periodStart(c.getPeriodStart())
                .periodEnd(c.getPeriodEnd())
                .objective(c.getObjective())
                .status(c.getStatus().name())
                .itemCount(itemCount)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    static ItemResponse toItemResponse(CalendarItem i) {
        return ItemResponse.builder()
                .id(i.getId())
                .calendarId(i.getCalendarId())
                .plannedDate(i.getPlannedDate())
                .channel(i.getChannel())
                .contentType(i.getContentType())
                .contentAngle(i.getContentAngle())
                .targetAudience(i.getTargetAudience())
                .hookDirection(i.getHookDirection())
                .ctaDirection(i.getCtaDirection())
                .status(i.getStatus().name())
                .generatedContentId(i.getGeneratedContentId())
                .workflowId(i.getWorkflowId())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    static BatchJobResponse toBatchJobResponse(BatchGenerationJob j) {
        return BatchJobResponse.builder()
                .id(j.getId())
                .calendarId(j.getCalendarId())
                .status(j.getStatus().name())
                .totalItems(j.getTotalItems())
                .completedItems(j.getCompletedItems())
                .failedItems(j.getFailedItems())
                .errorMessage(j.getErrorMessage())
                .createdAt(j.getCreatedAt())
                .updatedAt(j.getUpdatedAt())
                .build();
    }
}
