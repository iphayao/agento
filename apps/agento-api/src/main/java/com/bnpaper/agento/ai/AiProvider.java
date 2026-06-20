package com.bnpaper.agento.ai;

public interface AiProvider {

    AiContentResponse generateContent(AiContentRequest request);

    String getModelName();
}
