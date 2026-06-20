package com.bnpaper.agento.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agento.ai")
@Getter
@Setter
public class AiProperties {

    private String provider = "openai";
    private String baseUrl = "https://api.openai.com";
    private String apiKey = "";
    private String model = "gpt-4o-mini";
}
