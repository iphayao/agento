package com.bnpaper.agento.content;

import lombok.*;

import java.time.LocalDateTime;

public class GeneratedContentDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateRequest {
        private String contentType = "tiktok_caption";
        private String promptVersion = "v1";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long campaignId;
        private String contentType;
        private String channel;
        private String title;
        private String body;
        private String hook;
        private String callToAction;
        private String hashtags;
        private ContentStatus status;
        private String aiModel;
        private String promptVersion;
        private String complianceNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(GeneratedContent entity) {
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
}
