package com.bnpaper.agento.export;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ExportJobDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentExportRequest {

        @NotNull(message = "exportType is required")
        private ExportType exportType;

        private UUID campaignId;
        private String channel;
        private LocalDate startDate;
        private LocalDate endDate;

        @Builder.Default
        private List<String> statuses = List.of("APPROVED");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportJobResponse {
        private String id;
        private String exportType;
        private String status;
        private String calendarId;
        private String campaignId;
        private String channel;
        private String startDate;
        private String endDate;
        private String fileUrl;
        private String fileName;
        private Integer rowCount;
        private String errorMessage;
        private String createdAt;
        private String updatedAt;
    }

    static ExportJobResponse toResponse(ExportJob job, String downloadUrl) {
        return ExportJobResponse.builder()
                .id(job.getId().toString())
                .exportType(job.getExportType().name())
                .status(job.getStatus().name())
                .calendarId(job.getCalendarId() != null ? job.getCalendarId().toString() : null)
                .campaignId(job.getCampaignId() != null ? job.getCampaignId().toString() : null)
                .channel(job.getChannel())
                .startDate(job.getStartDate() != null ? job.getStartDate().toString() : null)
                .endDate(job.getEndDate() != null ? job.getEndDate().toString() : null)
                .fileUrl(job.getStatus() == ExportStatus.COMPLETED ? downloadUrl : null)
                .fileName(job.getFileName())
                .rowCount(job.getRowCount())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt() != null ? job.getCreatedAt().toString() : null)
                .updatedAt(job.getUpdatedAt() != null ? job.getUpdatedAt().toString() : null)
                .build();
    }
}
