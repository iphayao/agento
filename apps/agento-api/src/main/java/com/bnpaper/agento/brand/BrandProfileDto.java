package com.bnpaper.agento.brand;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BrandProfileDto {

    @Data
    public static class Request {
        @NotBlank(message = "Brand name is required")
        private String brandName;
        private String slogan;
        private String toneOfVoice;
        private String targetAudience;
        private List<String> keyMessages = new ArrayList<>();
        private List<String> prohibitedClaims = new ArrayList<>();
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private String brandName;
        private String slogan;
        private String toneOfVoice;
        private String targetAudience;
        private List<String> keyMessages;
        private List<String> prohibitedClaims;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    static Response toResponse(BrandProfile entity) {
        return Response.builder()
                .id(entity.getId())
                .brandName(entity.getBrandName())
                .slogan(entity.getSlogan())
                .toneOfVoice(entity.getToneOfVoice())
                .targetAudience(entity.getTargetAudience())
                .keyMessages(entity.getKeyMessages())
                .prohibitedClaims(entity.getProhibitedClaims())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
