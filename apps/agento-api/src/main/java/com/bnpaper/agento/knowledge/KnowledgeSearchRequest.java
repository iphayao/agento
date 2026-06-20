package com.bnpaper.agento.knowledge;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KnowledgeSearchRequest {

    @NotBlank(message = "Query is required")
    private String query;

    private DocumentType documentType;

    @Min(1) @Max(20)
    private int topK = 5;

    @Min(0) @Max(1)
    private double minScore = 0.0;
}
