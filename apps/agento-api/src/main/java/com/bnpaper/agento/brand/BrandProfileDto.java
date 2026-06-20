package com.bnpaper.agento.brand;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BrandProfileDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Brand name is required")
        private String brandName;
        private String slogan;
        private String toneOfVoice;
        private String targetAudience;
        @Builder.Default
        private List<String> keyMessages = new ArrayList<>();
        @Builder.Default
        private List<String> prohibitedClaims = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String brandName;
        private String slogan;
        private String toneOfVoice;
        private String targetAudience;
        private List<String> keyMessages;
        private List<String> prohibitedClaims;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(BrandProfile entity) {
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
}
