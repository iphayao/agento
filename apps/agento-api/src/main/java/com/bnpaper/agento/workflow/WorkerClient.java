package com.bnpaper.agento.workflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerClient {

    private final WorkerProperties properties;

    /**
     * Dispatches an agent workflow to the Python worker. The worker accepts the request,
     * starts background execution, and calls back to Spring Boot as each step completes.
     */
    public void dispatch(AgentWorkflowDto.DispatchRequest request) {
        RestClient client = buildClient();
        try {
            DispatchResponse response = client.post()
                    .uri(URI.create(properties.getBaseUrl() + "/workflows/content-generation"))
                    .body(request)
                    .retrieve()
                    .body(DispatchResponse.class);
            log.info("Worker accepted workflow {} — response: {}", request.getWorkflowId(),
                    response != null ? response.getMessage() : "null");
        } catch (RestClientException e) {
            throw new WorkerUnavailableException(
                    "agento-worker is unreachable at " + properties.getBaseUrl() + ": " + e.getMessage(), e);
        }
    }

    private RestClient buildClient() {
        return RestClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .requestInitializer(req -> {
                    req.getHeaders().set("X-Api-Key", "internal");
                })
                .build();
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DispatchResponse {
        private String workflowId;
        private String message;
    }

    static class WorkerUnavailableException extends RuntimeException {
        WorkerUnavailableException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
