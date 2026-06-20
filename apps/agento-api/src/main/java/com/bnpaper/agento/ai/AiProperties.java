package com.bnpaper.agento.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "agento.ai")
public class AiProperties {

    private String apiKey = "";
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o-mini";
    private double temperature = 0.7;
    private int maxTokens = 2000;
    private int connectTimeoutSeconds = 5;
    private int readTimeoutSeconds = 60;
}
