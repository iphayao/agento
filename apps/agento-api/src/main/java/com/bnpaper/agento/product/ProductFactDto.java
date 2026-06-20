package com.bnpaper.agento.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductFactDto {

    @Data
    public static class Request {
        @NotBlank(message = "Product name is required")
        private String productName;
        private String sku;
        private int sheetCount = 180;
        private int ply = 2;
        private int packSize = 5;
        private int cartonSize = 50;
        private List<String> keyBenefits = new ArrayList<>();
        private List<String> proofPoints = new ArrayList<>();
    }

    @Data
    @Builder
    public static class Response {
        private UUID id;
        private String productName;
        private String sku;
        private int sheetCount;
        private int ply;
        private int packSize;
        private int cartonSize;
        private List<String> keyBenefits;
        private List<String> proofPoints;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    static Response toResponse(ProductFact entity) {
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
