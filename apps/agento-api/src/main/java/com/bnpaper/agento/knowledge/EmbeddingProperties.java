package com.bnpaper.agento.knowledge;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "agento.embedding")
public class EmbeddingProperties {
    private String provider = "openai";
    private String model = "text-embedding-ada-002";
    private String apiKey = "";
    private String baseUrl = "https://api.openai.com/v1";
    private int dimensions = 1536;
    private int connectTimeoutSeconds = 5;
    private int readTimeoutSeconds = 30;
}
