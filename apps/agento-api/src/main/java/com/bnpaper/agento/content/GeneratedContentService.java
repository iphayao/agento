package com.bnpaper.agento.content;

import com.bnpaper.agento.ai.AiContentRequest;
import com.bnpaper.agento.ai.AiContentResponse;
import com.bnpaper.agento.ai.AiProperties;
import com.bnpaper.agento.ai.AiProvider;
import com.bnpaper.agento.ai.ComplianceChecker;
import com.bnpaper.agento.brand.BrandProfile;
import com.bnpaper.agento.brand.BrandProfileRepository;
import com.bnpaper.agento.campaign.Campaign;
import com.bnpaper.agento.campaign.CampaignRepository;
import com.bnpaper.agento.common.exception.AiProviderException;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.product.ProductFact;
import com.bnpaper.agento.product.ProductFactRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeneratedContentService {

    private final GeneratedContentRepository contentRepository;
    private final CampaignRepository campaignRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final ProductFactRepository productFactRepository;
    private final AiProvider aiProvider;
    private final AiProperties aiProperties;
    private final ComplianceChecker complianceChecker;
    private final ObjectMapper objectMapper;

    public List<GeneratedContentDto.Response> findAll() {
        return contentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(GeneratedContentDto::toResponse)
                .collect(Collectors.toList());
    }

    public List<GeneratedContentDto.Response> findByCampaignId(UUID campaignId) {
        return contentRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId).stream()
                .map(GeneratedContentDto::toResponse)
                .collect(Collectors.toList());
    }

    public GeneratedContentDto.Response findById(UUID id) {
        return contentRepository.findById(id)
                .map(GeneratedContentDto::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("GeneratedContent", id));
    }

    @Transactional
    public GeneratedContentDto.Response generate(UUID campaignId, GeneratedContentDto.GenerateRequest request) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", campaignId));

        BrandProfile brand = brandProfileRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No brand profile found. Please create a brand profile before generating content."));

        List<ProductFact> products = productFactRepository.findAll();

        String systemPrompt = buildSystemPrompt(brand, products);
        String userPrompt = buildUserPrompt(campaign, request.getContentType(), request.getAdditionalContext());

        AiContentRequest aiRequest = AiContentRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .model(aiProperties.getModel())
                .temperature(aiProperties.getTemperature())
                .maxTokens(aiProperties.getMaxTokens())
                .build();

        log.info("Generating {} content for campaign '{}'", request.getContentType(), campaign.getName());

        AiContentResponse aiResponse;
        try {
            aiResponse = aiProvider.generate(aiRequest);
        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new AiProviderException("Unexpected AI provider error", e);
        }

        ContentGenerationResult result = parseAndValidate(aiResponse.getContent());

        // Server-side compliance check on top of AI self-check
        String fullText = nullToEmpty(result.getTitle()) + " "
                + nullToEmpty(result.getHook()) + " "
                + nullToEmpty(result.getBody());
        List<String> serverFound = complianceChecker.findProhibitedTerms(fullText);
        List<String> aiFound = result.getProhibitedTermsDetected() != null
                ? result.getProhibitedTermsDetected() : List.of();

        List<String> allFound = new ArrayList<>(serverFound);
        aiFound.stream().filter(t -> !allFound.contains(t)).forEach(allFound::add);

        String complianceNotes = allFound.isEmpty()
                ? "Claim-safe — no prohibited terms detected"
                : "WARNING — prohibited terms detected: " + String.join(", ", allFound);

        GeneratedContent content = GeneratedContent.builder()
                .campaignId(campaignId)
                .contentType(request.getContentType())
                .channel(campaign.getChannel())
                .title(defaultIfBlank(result.getTitle(), campaign.getName() + " Content"))
                .body(result.getBody())
                .hook(result.getHook())
                .callToAction(result.getCallToAction())
                .hashtags(result.getHashtags() != null ? result.getHashtags() : List.of())
                .status(ContentStatus.DRAFT)
                .aiModel(aiResponse.getModel())
                .promptVersion("v1")
                .complianceNotes(complianceNotes)
                .build();

        GeneratedContent saved = contentRepository.save(content);
        log.info("Saved generated content {} as DRAFT", saved.getId());
        return GeneratedContentDto.toResponse(saved);
    }

    @Transactional
    public GeneratedContentDto.Response approve(UUID id) {
        GeneratedContent content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GeneratedContent", id));
        content.setStatus(ContentStatus.APPROVED);
        return GeneratedContentDto.toResponse(contentRepository.save(content));
    }

    @Transactional
    public GeneratedContentDto.Response reject(UUID id) {
        GeneratedContent content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GeneratedContent", id));
        content.setStatus(ContentStatus.REJECTED);
        return GeneratedContentDto.toResponse(contentRepository.save(content));
    }

    @Transactional
    public void delete(UUID id) {
        if (!contentRepository.existsById(id)) {
            throw new ResourceNotFoundException("GeneratedContent", id);
        }
        contentRepository.deleteById(id);
    }

    private String buildSystemPrompt(BrandProfile brand, List<ProductFact> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a Thai marketing content creator for ").append(brand.getBrandName())
                .append(", a facial tissue brand.\n\n");

        sb.append("## Brand Profile\n");
        sb.append("- Brand: ").append(brand.getBrandName()).append("\n");
        if (notBlank(brand.getSlogan())) {
            sb.append("- Slogan: ").append(brand.getSlogan()).append("\n");
        }
        if (notBlank(brand.getToneOfVoice())) {
            sb.append("- Tone of voice: ").append(brand.getToneOfVoice()).append("\n");
        }
        if (notBlank(brand.getTargetAudience())) {
            sb.append("- Target audience: ").append(brand.getTargetAudience()).append("\n");
        }
        if (brand.getKeyMessages() != null && !brand.getKeyMessages().isEmpty()) {
            sb.append("- Key messages: ").append(String.join("; ", brand.getKeyMessages())).append("\n");
        }

        if (!products.isEmpty()) {
            sb.append("\n## Product Information\n");
            for (ProductFact p : products) {
                sb.append("- ").append(p.getProductName());
                if (notBlank(p.getSku())) sb.append(" (SKU: ").append(p.getSku()).append(")");
                sb.append("\n");
                sb.append("  * ").append(p.getPly()).append("-ply, ").append(p.getSheetCount())
                        .append(" sheets/box, Pack of ").append(p.getPackSize())
                        .append(" boxes, Carton of ").append(p.getCartonSize()).append(" packs\n");
                if (p.getKeyBenefits() != null && !p.getKeyBenefits().isEmpty()) {
                    sb.append("  * Benefits: ").append(String.join(", ", p.getKeyBenefits())).append("\n");
                }
            }
        }

        sb.append("""

## CRITICAL — CLAIM SAFETY RULES (NON-NEGOTIABLE)
NEVER use these prohibited terms in any output:

THAI PROHIBITED: ไร้ฝุ่น 100%, ไม่มีฝุ่นเลย, ปลอดฝุ่นสมบูรณ์แบบ, antibacterial, ฆ่าเชื้อโรค,
medically safe, ปลอดภัยทางการแพทย์, hypoallergenic, dermatologist, ทดสอบโดยแพทย์,
สะอาดที่สุด, นุ่มที่สุด, ปลอดภัยที่สุด, ดีที่สุด, อันดับ 1, ขายดีที่สุด

ENGLISH PROHIBITED: dust-free, zero dust, 100% dust, antibacterial, medically safe,
hypoallergenic, dermatologist, safest, cleanest, softest, best tissue

APPROVED ALTERNATIVES:
- ฝุ่นน้อย (not ไร้ฝุ่น / dust-free)
- เนียนนุ่ม (not นุ่มที่สุด / softest)
- ให้สัมผัสสะอาด (not antibacterial / สะอาดที่สุด)
- เหมาะกับการใช้งานทุกวัน (not medically safe)
- คุ้มค่า (not ดีที่สุด / best)

## Output Format
Respond ONLY with a valid JSON object. No prose, no markdown fences. Schema:
{
  "title": "string — working title for internal reference",
  "hook": "string — attention-grabbing opening line (1–2 sentences in Thai)",
  "body": "string — main content body (Thai, conversational register)",
  "callToAction": "string — CTA phrase",
  "hashtags": ["#SoClean", "#ทิชชู่SoClean", "..."],
  "complianceNotes": "string — which approved claims were used",
  "prohibitedTermsDetected": []
}
""");

        if (brand.getProhibitedClaims() != null && !brand.getProhibitedClaims().isEmpty()) {
            sb.append("Additional brand-specific prohibited claims: ")
                    .append(String.join(", ", brand.getProhibitedClaims())).append("\n");
        }

        return sb.toString();
    }

    private String buildUserPrompt(Campaign campaign, String contentType, String additionalContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate ").append(contentType).append(" content for the following campaign:\n\n");
        sb.append("Campaign: ").append(campaign.getName()).append("\n");
        if (notBlank(campaign.getObjective())) {
            sb.append("Objective: ").append(campaign.getObjective()).append("\n");
        }
        if (notBlank(campaign.getChannel())) {
            sb.append("Channel: ").append(campaign.getChannel()).append("\n");
        }
        if (notBlank(campaign.getTargetAudience())) {
            sb.append("Target audience: ").append(campaign.getTargetAudience()).append("\n");
        }
        if (notBlank(campaign.getContentAngle())) {
            sb.append("Content angle: ").append(campaign.getContentAngle()).append("\n");
        }
        if (notBlank(additionalContext)) {
            sb.append("Additional context: ").append(additionalContext).append("\n");
        }
        sb.append("""

Requirements:
1. Write in Thai (ภาษาพูด — conversational register).
2. Content must suit the channel specified above.
3. Focus on the content angle provided.
4. Check every word against claim safety rules before outputting.
5. hashtags must always include #SoClean #ทิชชู่SoClean #BNPaper.
6. Return ONLY the JSON object — no prose, no markdown.
""");
        return sb.toString();
    }

    private ContentGenerationResult parseAndValidate(String jsonString) {
        try {
            ContentGenerationResult result = objectMapper.readValue(jsonString, ContentGenerationResult.class);
            if (result.getBody() == null || result.getBody().isBlank()) {
                throw new AiProviderException("AI response is missing required field 'body'");
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new AiProviderException("AI returned invalid JSON. Response: "
                    + jsonString.substring(0, Math.min(200, jsonString.length())));
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ContentGenerationResult {
        private String title;
        private String hook;
        private String body;
        private String callToAction;
        private List<String> hashtags;
        private String complianceNotes;
        private List<String> prohibitedTermsDetected;
    }
}
