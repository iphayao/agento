package com.bnpaper.agento.campaign;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class CampaignDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Campaign name is required")
        private String name;
        private String objective;
        @NotBlank(message = "Channel is required")
        private String channel;
        private String targetAudience;
        private String contentAngle;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String objective;
        private String channel;
        private String targetAudience;
        private String contentAngle;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Campaign entity) {
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
}
