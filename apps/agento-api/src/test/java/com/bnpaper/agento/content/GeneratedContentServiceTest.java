package com.bnpaper.agento.content;

import com.bnpaper.agento.ai.AiContentResponse;
import com.bnpaper.agento.ai.AiProvider;
import com.bnpaper.agento.ai.ComplianceChecker;
import com.bnpaper.agento.brand.BrandProfile;
import com.bnpaper.agento.brand.BrandProfileRepository;
import com.bnpaper.agento.campaign.Campaign;
import com.bnpaper.agento.campaign.CampaignService;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.product.ProductFactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneratedContentServiceTest {

    @Mock
    private GeneratedContentRepository repository;
    @Mock
    private CampaignService campaignService;
    @Mock
    private BrandProfileRepository brandProfileRepository;
    @Mock
    private ProductFactRepository productFactRepository;
    @Mock
    private AiProvider aiProvider;
    @Mock
    private ComplianceChecker complianceChecker;

    @InjectMocks
    private GeneratedContentService service;

    @Test
    void approve_changesStatusToApproved() {
        GeneratedContent content = GeneratedContent.builder()
                .id(1L)
                .campaignId(1L)
                .status(ContentStatus.DRAFT)
                .body("Test content")
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(content));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GeneratedContentDto.Response result = service.approve(1L);
        assertThat(result.getStatus()).isEqualTo(ContentStatus.APPROVED);
    }

    @Test
    void reject_changesStatusToRejected() {
        GeneratedContent content = GeneratedContent.builder()
                .id(1L)
                .campaignId(1L)
                .status(ContentStatus.DRAFT)
                .body("Test content")
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(content));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GeneratedContentDto.Response result = service.reject(1L);
        assertThat(result.getStatus()).isEqualTo(ContentStatus.REJECTED);
    }

    @Test
    void approve_throwsWhenContentNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.approve(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateForCampaign_throwsWhenNoBrandProfile() {
        Campaign campaign = Campaign.builder().id(1L).name("TikTok").channel("TikTok").build();
        when(campaignService.findById(1L)).thenReturn(campaign);
        when(brandProfileRepository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateForCampaign(1L, new GeneratedContentDto.GenerateRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brand profile");
    }

    @Test
    void generateForCampaign_savedWithDraftStatusAndComplianceNotes() {
        Campaign campaign = Campaign.builder().id(1L).name("TikTok").channel("TikTok").build();
        BrandProfile brand = BrandProfile.builder().id(1L).brandName("SoClean").build();

        AiContentResponse aiResponse = AiContentResponse.builder()
                .title("เนียนนุ่มทุกวัน")
                .hook("เคยรำคาญฝุ่นทิชชู่ไหม?")
                .body("SoClean ฝุ่นน้อย เนียนนุ่ม")
                .callToAction("สั่งได้เลยที่ TikTok Shop")
                .hashtags("#SoClean #ทิชชู่SoClean")
                .build();

        when(campaignService.findById(1L)).thenReturn(campaign);
        when(brandProfileRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(brand));
        when(productFactRepository.findAll()).thenReturn(List.of());
        when(aiProvider.getModelName()).thenReturn("gpt-4o-mini");
        when(aiProvider.generateContent(any())).thenReturn(aiResponse);
        when(complianceChecker.check(any())).thenReturn(List.of("Prohibited term: dust-free"));
        when(repository.save(any())).thenAnswer(inv -> {
            GeneratedContent c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        GeneratedContentDto.GenerateRequest req = new GeneratedContentDto.GenerateRequest("tiktok_caption", "v1");
        GeneratedContentDto.Response result = service.generateForCampaign(1L, req);

        assertThat(result.getStatus()).isEqualTo(ContentStatus.DRAFT);
        assertThat(result.getComplianceNotes()).contains("dust-free");
        assertThat(result.getAiModel()).isEqualTo("gpt-4o-mini");
    }

    @Test
    void generateForCampaign_nullComplianceNotesWhenClean() {
        Campaign campaign = Campaign.builder().id(1L).name("TikTok").channel("TikTok").build();
        BrandProfile brand = BrandProfile.builder().id(1L).brandName("SoClean").build();

        AiContentResponse aiResponse = AiContentResponse.builder()
                .body("สะอาด เนียนนุ่ม ฝุ่นน้อย")
                .build();

        when(campaignService.findById(1L)).thenReturn(campaign);
        when(brandProfileRepository.findTopByOrderByIdAsc()).thenReturn(Optional.of(brand));
        when(productFactRepository.findAll()).thenReturn(List.of());
        when(aiProvider.getModelName()).thenReturn("gpt-4o-mini");
        when(aiProvider.generateContent(any())).thenReturn(aiResponse);
        when(complianceChecker.check(any())).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(inv -> {
            GeneratedContent c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        GeneratedContentDto.Response result = service.generateForCampaign(1L, new GeneratedContentDto.GenerateRequest());
        assertThat(result.getComplianceNotes()).isNull();
    }
}
