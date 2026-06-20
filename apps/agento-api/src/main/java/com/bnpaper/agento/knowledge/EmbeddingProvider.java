package com.bnpaper.agento.knowledge;

public interface EmbeddingProvider {

    /**
     * Embed a text string and return the float vector.
     * Returns null if the provider is not configured or the call fails.
     */
    float[] embed(String text);

    String providerName();

    int dimensions();
}
