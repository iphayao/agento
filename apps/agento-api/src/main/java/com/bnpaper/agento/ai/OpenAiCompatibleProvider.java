package com.bnpaper.agento.ai;

import com.bnpaper.agento.common.exception.AiProviderException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleProvider implements AiProvider {

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Override
    public AiContentResponse generateContent(AiContentRequest request) {
        String systemPrompt = buildSystemPrompt(request);
        String userPrompt = buildUserPrompt(request);

        ChatRequest chatRequest = new ChatRequest(
                properties.getModel(),
                List.of(
                        new Message("system", systemPrompt),
                        new Message("user", userPrompt)
                ),
                new ResponseFormat("json_object"),
                0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        String url = properties.getBaseUrl() + "/v1/chat/completions";

        try {
            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(chatRequest, headers),
                    ChatResponse.class
            );

            if (response.getBody() == null || response.getBody().getChoices().isEmpty()) {
                throw new AiProviderException("AI provider returned empty response");
            }

            String content = response.getBody().getChoices().get(0).getMessage().content();
            log.debug("AI raw response: {}", content);

            AiContentResponse aiResponse = objectMapper.readValue(content, AiContentResponse.class);

            if (!aiResponse.isValid()) {
                throw new AiProviderException("AI response is missing required 'body' field");
            }

            return aiResponse;

        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI provider call failed", e);
            throw new AiProviderException("AI provider error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getModelName() {
        return properties.getModel();
    }

    private String buildSystemPrompt(AiContentRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a Thai marketing content expert for SoClean facial tissue brand by BN Paper.\n\n");
        sb.append("## Brand Context\n");
        sb.append("Brand: ").append(req.getBrandName()).append("\n");
        if (req.getSlogan() != null) sb.append("Slogan: ").append(req.getSlogan()).append("\n");
        if (req.getToneOfVoice() != null) sb.append("Tone: ").append(req.getToneOfVoice()).append("\n");
        if (req.getBrandTargetAudience() != null) sb.append("Brand audience: ").append(req.getBrandTargetAudience()).append("\n");

        if (req.getKeyMessages() != null && !req.getKeyMessages().isEmpty()) {
            sb.append("\n## Key Messages\n");
            req.getKeyMessages().forEach(m -> sb.append("- ").append(m).append("\n"));
        }

        sb.append("\n## Product Facts\n");
        sb.append("Product: ").append(req.getProductName()).append("\n");
        if (req.getPly() != null) sb.append("Ply: ").append(req.getPly()).append("-ply\n");
        if (req.getSheetCount() != null) sb.append("Sheets per box: ").append(req.getSheetCount()).append("\n");
        if (req.getPackSize() != null) sb.append("Pack size: ").append(req.getPackSize()).append(" boxes\n");
        if (req.getCartonSize() != null) sb.append("Carton: ").append(req.getCartonSize()).append(" packs\n");

        if (req.getKeyBenefits() != null && !req.getKeyBenefits().isEmpty()) {
            sb.append("Key benefits:\n");
            req.getKeyBenefits().forEach(b -> sb.append("- ").append(b).append("\n"));
        }
        if (req.getProofPoints() != null && !req.getProofPoints().isEmpty()) {
            sb.append("Proof points:\n");
            req.getProofPoints().forEach(p -> sb.append("- ").append(p).append("\n"));
        }

        sb.append("\n## COMPLIANCE RULES — STRICTLY FOLLOW\n");
        sb.append("NEVER use these prohibited terms:\n");
        List<String> prohibited = List.of(
                "ไร้ฝุ่น 100%", "ไม่มีฝุ่นเลย", "ปลอดฝุ่นสมบูรณ์แบบ",
                "antibacterial", "ฆ่าเชื้อโรค", "medically safe", "ปลอดภัยทางการแพทย์",
                "hypoallergenic", "dermatologist", "ทดสอบโดยแพทย์",
                "สะอาดที่สุด", "นุ่มที่สุด", "ปลอดภัยที่สุด", "ดีที่สุด",
                "อันดับ 1", "ขายดีที่สุด", "dust-free", "zero dust",
                "safest", "cleanest", "softest", "best tissue", "100% dust"
        );
        if (req.getProhibitedClaims() != null) {
            prohibited = prohibited.stream()
                    .collect(Collectors.toList());
            prohibited.addAll(req.getProhibitedClaims());
        }
        prohibited.forEach(t -> sb.append("- ").append(t).append("\n"));

        sb.append("\nUSE ONLY approved safe claims:\n");
        sb.append("- ฝุ่นน้อย (not ไร้ฝุ่น)\n");
        sb.append("- เนียนนุ่ม (not นุ่มที่สุด)\n");
        sb.append("- ให้สัมผัสสะอาด\n");
        sb.append("- เหมาะกับการใช้งานทุกวัน\n");
        sb.append("- คุ้มค่าสำหรับบ้าน ร้านค้า และออฟฟิศ\n");

        sb.append("\n## Output Format\n");
        sb.append("Respond ONLY with valid JSON in this exact structure:\n");
        sb.append("{\n");
        sb.append("  \"title\": \"short headline (max 60 chars, Thai preferred)\",\n");
        sb.append("  \"hook\": \"attention-grabbing opening line\",\n");
        sb.append("  \"body\": \"main content copy following brand tone\",\n");
        sb.append("  \"callToAction\": \"what the audience should do next\",\n");
        sb.append("  \"hashtags\": \"#SoClean #ทิชชู่SoClean plus 3-5 relevant hashtags\"\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String buildUserPrompt(AiContentRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate ").append(req.getContentType()).append(" content for this campaign:\n\n");
        sb.append("Campaign: ").append(req.getCampaignName()).append("\n");
        if (req.getCampaignObjective() != null) sb.append("Objective: ").append(req.getCampaignObjective()).append("\n");
        sb.append("Channel: ").append(req.getChannel()).append("\n");
        if (req.getCampaignTargetAudience() != null) sb.append("Target audience: ").append(req.getCampaignTargetAudience()).append("\n");
        if (req.getContentAngle() != null) sb.append("Content angle: ").append(req.getContentAngle()).append("\n");
        sb.append("\nGenerate compelling, compliant Thai marketing content for ").append(req.getChannel()).append(". ");
        sb.append("Match the SoClean brand voice: warm, honest, practical. Return only JSON.");
        return sb.toString();
    }

    // --- Internal OpenAI API DTOs ---

    record ChatRequest(String model, List<Message> messages, ResponseFormat response_format, double temperature) {}

    record Message(String role, String content) {}

    record ResponseFormat(String type) {}

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatResponse {
        private List<Choice> choices;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        private Message message;
    }
}
