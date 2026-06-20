package com.bnpaper.agento.content;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class GeneratedContentDto {

    @Data
    public static class GenerateRequest {
        @NotBlank(message = "Content type is required")
        private String contentType;
        private String additionalContext;
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private UUID campaignId;
        private String contentType;
        private String channel;
        private String title;
        private String body;
        private String hook;
        private String callToAction;
        private List<String> hashtags;
        private ContentStatus status;
        private String aiModel;
        private String promptVersion;
        private String complianceNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    static Response toResponse(GeneratedContent entity) {
        return Response.builder()
                .id(entity.getId())
                .campaignId(entity.getCampaignId())
                .contentType(entity.getContentType())
                .channel(entity.getChannel())
                .title(entity.getTitle())
                .body(entity.getBody())
                .hook(entity.getHook())
                .callToAction(entity.getCallToAction())
                .hashtags(entity.getHashtags())
                .status(entity.getStatus())
                .aiModel(entity.getAiModel())
                .promptVersion(entity.getPromptVersion())
                .complianceNotes(entity.getComplianceNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
