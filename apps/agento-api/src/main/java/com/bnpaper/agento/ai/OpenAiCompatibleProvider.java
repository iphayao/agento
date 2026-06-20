package com.bnpaper.agento.ai;

import com.bnpaper.agento.common.exception.AiProviderException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleProvider implements AiProvider {

    private final RestClient aiRestClient;
    private final AiProperties properties;

    @Override
    public AiContentResponse generate(AiContentRequest request) {
        String model = request.getModel() != null ? request.getModel() : properties.getModel();

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setModel(model);
        chatRequest.setMessages(List.of(
                new Message("system", request.getSystemPrompt()),
                new Message("user", request.getUserPrompt())
        ));
        chatRequest.setTemperature(request.getTemperature());
        chatRequest.setMaxTokens(request.getMaxTokens());
        chatRequest.setResponseFormat(new ResponseFormat("json_object"));

        log.debug("Calling AI provider at {} with model {}", properties.getBaseUrl(), model);

        ChatResponse response;
        try {
            response = aiRestClient.post()
                    .uri("/chat/completions")
                    .body(chatRequest)
                    .retrieve()
                    .body(ChatResponse.class);
        } catch (RestClientException e) {
            throw new AiProviderException("AI API call failed: " + e.getMessage(), e);
        }

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new AiProviderException("AI API returned empty response");
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        if (content == null || content.isBlank()) {
            throw new AiProviderException("AI API returned empty content");
        }

        return AiContentResponse.builder()
                .content(content)
                .model(response.getModel() != null ? response.getModel() : model)
                .promptTokens(response.getUsage() != null ? response.getUsage().getPromptTokens() : 0)
                .completionTokens(response.getUsage() != null ? response.getUsage().getCompletionTokens() : 0)
                .build();
    }

    @Data
    @NoArgsConstructor
    static class ChatRequest {
        private String model;
        private List<Message> messages;
        private double temperature;
        @JsonProperty("max_tokens")
        private int maxTokens;
        @JsonProperty("response_format")
        private ResponseFormat responseFormat;
    }

    @Data
    @NoArgsConstructor
    static class Message {
        private String role;
        private String content;

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatResponse {
        private String model;
        private List<Choice> choices;
        private Usage usage;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        private Message message;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
    }

    @Data
    @NoArgsConstructor
    static class ResponseFormat {
        private String type;

        ResponseFormat(String type) {
            this.type = type;
        }
    }
}
