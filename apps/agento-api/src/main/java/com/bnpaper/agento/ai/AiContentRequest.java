package com.bnpaper.agento.ai;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiContentRequest {

    private String systemPrompt;
    private String userPrompt;
    private String model;

    @Builder.Default
    private double temperature = 0.7;

    @Builder.Default
    private int maxTokens = 2000;
}
