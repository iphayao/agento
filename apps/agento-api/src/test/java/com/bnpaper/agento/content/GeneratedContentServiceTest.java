package com.bnpaper.agento.content;

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
import com.bnpaper.agento.product.ProductFactRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneratedContentServiceTest {

    @Mock
    private GeneratedContentRepository contentRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private BrandProfileRepository brandProfileRepository;
    @Mock
    private ProductFactRepository productFactRepository;
    @Mock
    private AiProvider aiProvider;
    @Mock
    private AiProperties aiProperties;
    @Mock
    private ComplianceChecker complianceChecker;

    @InjectMocks
    private GeneratedContentService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID campaignId;
    private Campaign campaign;
    private BrandProfile brand;

    @BeforeEach
    void setUp() throws Exception {
        // Inject real ObjectMapper via reflection since @InjectMocks doesn't work for non-mocked deps
        var field = GeneratedContentService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(service, objectMapper);

        campaignId = UUID.randomUUID();
        campaign = Campaign.builder()
                .id(campaignId)
                .name("TikTok June Campaign")
                .channel("tiktok")
                .targetAudience("Women Gen Y")
                .contentAngle("ฝุ่นน้อย")
                .status("ACTIVE")
                .build();

        brand = BrandProfile.builder()
                .id(UUID.randomUUID())
                .brandName("SoClean")
                .slogan("สะอาด เนียนนุ่ม ฝุ่นน้อย")
                .toneOfVoice("Warm, honest, practical")
                .keyMessages(List.of("ฝุ่นน้อย", "เนียนนุ่ม"))
                .prohibitedClaims(List.of())
                .build();
    }

    @Test
    void generate_successfullySavesAsDraft() {
        GeneratedContentDto.GenerateRequest request = new GeneratedContentDto.GenerateRequest();
        request.setContentType("TIKTOK_CAPTION");

        String validJson = """
                {
                  "title": "TikTok Caption June",
                  "hook": "ทิชชู่ฝุ่นน้อย เหมาะกับออฟฟิศ",
                  "body": "SoClean เนียนนุ่ม ฝุ่นน้อย ใช้ได้ทุกวัน",
                  "callToAction": "สั่งซื้อเลยที่ TikTok Shop",
                  "hashtags": ["#SoClean", "#ทิชชู่SoClean", "#BNPaper"],
                  "complianceNotes": "ใช้ฝุ่นน้อย และเนียนนุ่ม",
                  "prohibitedTermsDetected": []
                }
                """;

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(brandProfileRepository.findAll()).thenReturn(List.of(brand));
        when(productFactRepository.findAll()).thenReturn(List.of());
        when(aiProperties.getModel()).thenReturn("gpt-4o-mini");
        when(aiProperties.getTemperature()).thenReturn(0.7);
        when(aiProperties.getMaxTokens()).thenReturn(2000);
        when(aiProvider.generate(any())).thenReturn(
                AiContentResponse.builder().content(validJson).model("gpt-4o-mini").build());
        when(complianceChecker.findProhibitedTerms(any())).thenReturn(List.of());

        GeneratedContent savedContent = GeneratedContent.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .contentType("TIKTOK_CAPTION")
                .channel("tiktok")
                .title("TikTok Caption June")
                .body("SoClean เนียนนุ่ม ฝุ่นน้อย ใช้ได้ทุกวัน")
                .status(ContentStatus.DRAFT)
                .hashtags(List.of("#SoClean", "#ทิชชู่SoClean"))
                .aiModel("gpt-4o-mini")
                .promptVersion("v1")
                .complianceNotes("Claim-safe — no prohibited terms detected")
                .build();

        when(contentRepository.save(any())).thenReturn(savedContent);

        GeneratedContentDto.Response result = service.generate(campaignId, request);

        assertThat(result.getStatus()).isEqualTo(ContentStatus.DRAFT);
        assertThat(result.getContentType()).isEqualTo("TIKTOK_CAPTION");
        verify(contentRepository).save(any());
    }

    @Test
    void generate_throwsWhenNoBrandProfile() {
        GeneratedContentDto.GenerateRequest request = new GeneratedContentDto.GenerateRequest();
        request.setContentType("TIKTOK_CAPTION");

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(brandProfileRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.generate(campaignId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("brand profile");
    }

    @Test
    void generate_throwsWhenAiReturnsInvalidJson() {
        GeneratedContentDto.GenerateRequest request = new GeneratedContentDto.GenerateRequest();
        request.setContentType("TIKTOK_CAPTION");

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(brandProfileRepository.findAll()).thenReturn(List.of(brand));
        when(productFactRepository.findAll()).thenReturn(List.of());
        when(aiProperties.getModel()).thenReturn("gpt-4o-mini");
        when(aiProperties.getTemperature()).thenReturn(0.7);
        when(aiProperties.getMaxTokens()).thenReturn(2000);
        when(aiProvider.generate(any())).thenReturn(
                AiContentResponse.builder().content("not valid json at all").model("gpt-4o-mini").build());

        assertThatThrownBy(() -> service.generate(campaignId, request))
                .isInstanceOf(AiProviderException.class)
                .hasMessageContaining("invalid JSON");

        verify(contentRepository, never()).save(any());
    }

    @Test
    void generate_flagsProhibitedTermsInComplianceNotes() {
        GeneratedContentDto.GenerateRequest request = new GeneratedContentDto.GenerateRequest();
        request.setContentType("TIKTOK_CAPTION");

        String jsonWithIssue = """
                {
                  "title": "Caption",
                  "hook": "test hook",
                  "body": "SoClean softest tissue",
                  "callToAction": "Buy now",
                  "hashtags": ["#SoClean"],
                  "complianceNotes": "used softest",
                  "prohibitedTermsDetected": ["softest"]
                }
                """;

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(brandProfileRepository.findAll()).thenReturn(List.of(brand));
        when(productFactRepository.findAll()).thenReturn(List.of());
        when(aiProperties.getModel()).thenReturn("gpt-4o-mini");
        when(aiProperties.getTemperature()).thenReturn(0.7);
        when(aiProperties.getMaxTokens()).thenReturn(2000);
        when(aiProvider.generate(any())).thenReturn(
                AiContentResponse.builder().content(jsonWithIssue).model("gpt-4o-mini").build());
        when(complianceChecker.findProhibitedTerms(any())).thenReturn(List.of("softest"));

        GeneratedContent savedContent = GeneratedContent.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .contentType("TIKTOK_CAPTION")
                .body("SoClean softest tissue")
                .status(ContentStatus.DRAFT)
                .hashtags(List.of())
                .complianceNotes("WARNING — prohibited terms detected: softest")
                .build();
        when(contentRepository.save(any())).thenReturn(savedContent);

        GeneratedContentDto.Response result = service.generate(campaignId, request);

        assertThat(result.getComplianceNotes()).contains("WARNING");
        assertThat(result.getStatus()).isEqualTo(ContentStatus.DRAFT);
    }

    @Test
    void approve_changesStatusToApproved() {
        UUID contentId = UUID.randomUUID();
        GeneratedContent draft = GeneratedContent.builder()
                .id(contentId)
                .campaignId(campaignId)
                .status(ContentStatus.DRAFT)
                .hashtags(List.of())
                .build();
        GeneratedContent approved = GeneratedContent.builder()
                .id(contentId)
                .campaignId(campaignId)
                .status(ContentStatus.APPROVED)
                .hashtags(List.of())
                .build();

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(draft));
        when(contentRepository.save(any())).thenReturn(approved);

        GeneratedContentDto.Response result = service.approve(contentId);

        assertThat(result.getStatus()).isEqualTo(ContentStatus.APPROVED);
    }

    @Test
    void reject_changesStatusToRejected() {
        UUID contentId = UUID.randomUUID();
        GeneratedContent draft = GeneratedContent.builder()
                .id(contentId)
                .campaignId(campaignId)
                .status(ContentStatus.DRAFT)
                .hashtags(List.of())
                .build();
        GeneratedContent rejected = GeneratedContent.builder()
                .id(contentId)
                .campaignId(campaignId)
                .status(ContentStatus.REJECTED)
                .hashtags(List.of())
                .build();

        when(contentRepository.findById(contentId)).thenReturn(Optional.of(draft));
        when(contentRepository.save(any())).thenReturn(rejected);

        GeneratedContentDto.Response result = service.reject(contentId);

        assertThat(result.getStatus()).isEqualTo(ContentStatus.REJECTED);
    }
}
