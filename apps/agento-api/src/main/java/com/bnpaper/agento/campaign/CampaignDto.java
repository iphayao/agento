package com.bnpaper.agento.campaign;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class CampaignDto {

    @Data
    public static class Request {
        @NotBlank(message = "Campaign name is required")
        private String name;
        private String objective;
        private String channel;
        private String targetAudience;
        private String contentAngle;
        private String status = "DRAFT";
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private String name;
        private String objective;
        private String channel;
        private String targetAudience;
        private String contentAngle;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    static Response toResponse(Campaign entity) {
        return Response.builder()
                .id(entity.getId())
                .name(entity.getName())
                .objective(entity.getObjective())
                .channel(entity.getChannel())
                .targetAudience(entity.getTargetAudience())
                .contentAngle(entity.getContentAngle())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
