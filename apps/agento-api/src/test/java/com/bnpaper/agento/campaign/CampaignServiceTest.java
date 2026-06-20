package com.bnpaper.agento.campaign;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
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
class CampaignServiceTest {

    @Mock
    private CampaignRepository repository;

    @InjectMocks
    private CampaignService service;

    private Campaign sampleCampaign;
    private UUID campaignId;

    @BeforeEach
    void setUp() {
        campaignId = UUID.randomUUID();
        sampleCampaign = Campaign.builder()
                .id(campaignId)
                .name("June TikTok Campaign")
                .objective("Drive TikTok Shop sales")
                .channel("tiktok")
                .targetAudience("Women Gen Y households")
                .contentAngle("ฝุ่นน้อย เหมาะกับออฟฟิศ")
                .status("DRAFT")
                .build();
    }

    @Test
    void findAll_returnsAllCampaigns() {
        when(repository.findAll()).thenReturn(List.of(sampleCampaign));

        List<CampaignDto.Response> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("June TikTok Campaign");
        assertThat(result.get(0).getChannel()).isEqualTo("tiktok");
    }

    @Test
    void findById_returnsCampaign() {
        when(repository.findById(campaignId)).thenReturn(Optional.of(sampleCampaign));

        CampaignDto.Response result = service.findById(campaignId);

        assertThat(result.getId()).isEqualTo(campaignId);
        assertThat(result.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void findById_throwsWhenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_persistsCampaign() {
        CampaignDto.Request request = new CampaignDto.Request();
        request.setName("New Campaign");
        request.setChannel("shopee");
        request.setObjective("Increase Shopee sales");

        when(repository.save(any(Campaign.class))).thenReturn(sampleCampaign);

        CampaignDto.Response result = service.create(request);

        assertThat(result).isNotNull();
        verify(repository, times(1)).save(any(Campaign.class));
    }

    @Test
    void create_defaultsStatusToDraft() {
        CampaignDto.Request request = new CampaignDto.Request();
        request.setName("Campaign Without Status");
        request.setStatus(null);

        when(repository.save(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            assertThat(c.getStatus()).isEqualTo("DRAFT");
            return sampleCampaign;
        });

        service.create(request);
    }

    @Test
    void update_modifiesCampaign() {
        CampaignDto.Request request = new CampaignDto.Request();
        request.setName("Updated Campaign");
        request.setStatus("ACTIVE");

        Campaign updated = Campaign.builder()
                .id(campaignId)
                .name("Updated Campaign")
                .status("ACTIVE")
                .build();

        when(repository.findById(campaignId)).thenReturn(Optional.of(sampleCampaign));
        when(repository.save(any(Campaign.class))).thenReturn(updated);

        CampaignDto.Response result = service.update(campaignId, request);

        assertThat(result.getName()).isEqualTo("Updated Campaign");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void delete_removesEntity() {
        when(repository.existsById(campaignId)).thenReturn(true);

        service.delete(campaignId);

        verify(repository).deleteById(campaignId);
    }

    @Test
    void findEntityById_returnsEntity() {
        when(repository.findById(campaignId)).thenReturn(Optional.of(sampleCampaign));

        Campaign result = service.findEntityById(campaignId);

        assertThat(result.getName()).isEqualTo("June TikTok Campaign");
    }
}
