package com.bnpaper.agento.performance;

import com.bnpaper.agento.workflow.WorkerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceWorkerClient {

    private final WorkerProperties properties;

    public void dispatchAnalysis(List<Map<String, Object>> records, String channel) {
        Map<String, Object> payload = Map.of(
                "records", records,
                "channel", channel != null ? channel : "all",
                "callbackBaseUrl", properties.getCallbackBaseUrl(),
                "callbackApiKey", properties.getApiKey()
        );
        try {
            buildClient().post()
                    .uri(URI.create(properties.getBaseUrl() + "/performance/analyze"))
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Dispatched performance analysis to worker ({} records)", records.size());
        } catch (RestClientException e) {
            log.error("Failed to dispatch performance analysis to worker: {}", e.getMessage());
            throw new PerformanceWorkerException("agento-worker unreachable: " + e.getMessage(), e);
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

    static class PerformanceWorkerException extends RuntimeException {
        PerformanceWorkerException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
