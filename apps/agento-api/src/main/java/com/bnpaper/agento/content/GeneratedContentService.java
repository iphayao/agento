package com.bnpaper.agento.content;

import com.bnpaper.agento.ai.AiContentRequest;
import com.bnpaper.agento.ai.AiContentResponse;
import com.bnpaper.agento.ai.AiProvider;
import com.bnpaper.agento.ai.ComplianceChecker;
import com.bnpaper.agento.brand.BrandProfile;
import com.bnpaper.agento.brand.BrandProfileRepository;
import com.bnpaper.agento.campaign.Campaign;
import com.bnpaper.agento.campaign.CampaignService;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.product.ProductFact;
import com.bnpaper.agento.product.ProductFactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratedContentService {

    private final GeneratedContentRepository repository;
    private final CampaignService campaignService;
    private final BrandProfileRepository brandProfileRepository;
    private final ProductFactRepository productFactRepository;
    private final AiProvider aiProvider;
    private final ComplianceChecker complianceChecker;

    @Transactional(readOnly = true)
    public List<GeneratedContentDto.Response> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(GeneratedContentDto.Response::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GeneratedContentDto.Response> findByCampaignId(Long campaignId) {
        return repository.findByCampaignIdOrderByCreatedAtDesc(campaignId).stream()
                .map(GeneratedContentDto.Response::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GeneratedContentDto.Response findById(Long id) {
        return GeneratedContentDto.Response.from(
                repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("GeneratedContent", id))
        );
    }

    @Transactional
    public GeneratedContentDto.Response generateForCampaign(Long campaignId,
            GeneratedContentDto.GenerateRequest generateRequest) {

        Campaign campaign = campaignService.findById(campaignId);
        BrandProfile brand = brandProfileRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException(
                        "No brand profile found. Please create a brand profile first."));
        List<ProductFact> products = productFactRepository.findAll();

        ProductFact primaryProduct = products.isEmpty() ? null : products.get(0);

        String contentType = generateRequest.getContentType() != null
                ? generateRequest.getContentType() : "tiktok_caption";
        String promptVersion = generateRequest.getPromptVersion() != null
                ? generateRequest.getPromptVersion() : "v1";

        AiContentRequest aiRequest = buildAiRequest(brand, primaryProduct, campaign, contentType, promptVersion);

        log.info("Generating content for campaign={} contentType={} model={}",
                campaignId, contentType, aiProvider.getModelName());

        AiContentResponse aiResponse = aiProvider.generateContent(aiRequest);

        List<String> flags = complianceChecker.check(aiResponse);
        String complianceNotes = flags.isEmpty() ? null
                : "Compliance flags: " + String.join("; ", flags);

        if (!flags.isEmpty()) {
            log.warn("Compliance flags for campaign={}: {}", campaignId, flags);
        }

        GeneratedContent content = GeneratedContent.builder()
                .campaignId(campaignId)
                .contentType(contentType)
                .channel(campaign.getChannel())
                .title(aiResponse.getTitle())
                .hook(aiResponse.getHook())
                .body(aiResponse.getBody())
                .callToAction(aiResponse.getCallToAction())
                .hashtags(aiResponse.getHashtags())
                .status(ContentStatus.DRAFT)
                .aiModel(aiProvider.getModelName())
                .promptVersion(promptVersion)
                .complianceNotes(complianceNotes)
                .build();

        return GeneratedContentDto.Response.from(repository.save(content));
    }

    @Transactional
    public GeneratedContentDto.Response approve(Long id) {
        GeneratedContent content = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GeneratedContent", id));
        content.setStatus(ContentStatus.APPROVED);
        return GeneratedContentDto.Response.from(repository.save(content));
    }

    @Transactional
    public GeneratedContentDto.Response reject(Long id) {
        GeneratedContent content = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GeneratedContent", id));
        content.setStatus(ContentStatus.REJECTED);
        return GeneratedContentDto.Response.from(repository.save(content));
    }

    private AiContentRequest buildAiRequest(BrandProfile brand, ProductFact product,
            Campaign campaign, String contentType, String promptVersion) {

        AiContentRequest.AiContentRequestBuilder builder = AiContentRequest.builder()
                .brandName(brand.getBrandName())
                .slogan(brand.getSlogan())
                .toneOfVoice(brand.getToneOfVoice())
                .brandTargetAudience(brand.getTargetAudience())
                .keyMessages(brand.getKeyMessages())
                .prohibitedClaims(brand.getProhibitedClaims())
                .campaignName(campaign.getName())
                .campaignObjective(campaign.getObjective())
                .channel(campaign.getChannel())
                .campaignTargetAudience(campaign.getTargetAudience())
                .contentAngle(campaign.getContentAngle())
                .contentType(contentType)
                .promptVersion(promptVersion);

        if (product != null) {
            builder.productName(product.getProductName())
                    .sheetCount(product.getSheetCount())
                    .ply(product.getPly())
                    .packSize(product.getPackSize())
                    .cartonSize(product.getCartonSize())
                    .keyBenefits(product.getKeyBenefits())
                    .proofPoints(product.getProofPoints());
        } else {
            builder.productName("SoClean Facial Tissue");
        }

        return builder.build();
    }
}
