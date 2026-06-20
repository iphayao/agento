package com.bnpaper.agento.knowledge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
public class OpenAiEmbeddingProvider implements EmbeddingProvider {

    private final EmbeddingProperties properties;
    private final RestClient restClient;

    public OpenAiEmbeddingProvider(EmbeddingProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public float[] embed(String text) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.debug("Embedding API key not configured — skipping embedding");
            return null;
        }
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            var body = Map.of("model", properties.getModel(), "input", text.strip());
            EmbeddingResponse response = restClient.post()
                    .uri("/embeddings")
                    .body(body)
                    .retrieve()
                    .body(EmbeddingResponse.class);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                List<Float> values = response.getData().get(0).getEmbedding();
                if (values != null) {
                    float[] result = new float[values.size()];
                    for (int i = 0; i < values.size(); i++) {
                        result[i] = values.get(i);
                    }
                    return result;
                }
            }
        } catch (RestClientException e) {
            log.warn("Embedding call failed: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String providerName() {
        return "openai-embedding";
    }

    @Override
    public int dimensions() {
        return properties.getDimensions();
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddingResponse {
        private List<EmbeddingData> data;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddingData {
        private List<Float> embedding;

        @JsonProperty("index")
        private int index;
    }
}
