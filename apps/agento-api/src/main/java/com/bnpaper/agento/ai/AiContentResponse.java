package com.bnpaper.agento.ai;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiContentResponse {

    private String content;
    private String model;
    private int promptTokens;
    private int completionTokens;
}
