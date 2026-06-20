package com.bnpaper.agento.workflow;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "agento.worker")
public class WorkerProperties {

    private String baseUrl = "http://localhost:8001";
    private int connectTimeoutSeconds = 5;
    private int readTimeoutSeconds = 120;
    /** Spring Boot's own base URL used in callbacks sent to the worker */
    private String callbackBaseUrl = "http://localhost:8080/api";
}
