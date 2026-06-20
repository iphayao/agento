package com.bnpaper.agento.product;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductFactDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Product name is required")
        private String productName;
        private String sku;
        private Integer sheetCount;
        private Integer ply;
        private Integer packSize;
        private Integer cartonSize;
        @Builder.Default
        private List<String> keyBenefits = new ArrayList<>();
        @Builder.Default
        private List<String> proofPoints = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String productName;
        private String sku;
        private Integer sheetCount;
        private Integer ply;
        private Integer packSize;
        private Integer cartonSize;
        private List<String> keyBenefits;
        private List<String> proofPoints;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(ProductFact entity) {
            return Response.builder()
                    .id(entity.getId())
                    .productName(entity.getProductName())
                    .sku(entity.getSku())
                    .sheetCount(entity.getSheetCount())
                    .ply(entity.getPly())
                    .packSize(entity.getPackSize())
                    .cartonSize(entity.getCartonSize())
                    .keyBenefits(entity.getKeyBenefits())
                    .proofPoints(entity.getProofPoints())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }
}
