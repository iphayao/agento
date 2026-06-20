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
import java.util.UUID;

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
        try {
            DispatchResponse response = buildClient().post()
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

    /**
     * Best-effort cancel signal to the worker. If the worker is unreachable the Spring Boot
     * cancel operation still completes — subsequent callbacks from the worker will be ignored.
     */
    public void cancelWorkflow(UUID workflowId) {
        try {
            buildClient().post()
                    .uri(URI.create(properties.getBaseUrl() + "/workflows/" + workflowId + "/cancel"))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Cancel signal sent to worker for workflow {}", workflowId);
        } catch (RestClientException e) {
            log.warn("Could not send cancel signal to worker for workflow {} (worker may be down): {}",
                    workflowId, e.getMessage());
        }
    }

    private RestClient buildClient() {
        RestClient.Builder builder = RestClient.builder()
                .defaultHeader("Content-Type", "application/json");
        String apiKey = properties.getApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("X-Api-Key", apiKey);
        }
        return builder.build();
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
