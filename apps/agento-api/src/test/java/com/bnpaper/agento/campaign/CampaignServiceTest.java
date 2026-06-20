package com.bnpaper.agento.campaign;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
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
class CampaignServiceTest {

    @Mock
    private CampaignRepository repository;

    @InjectMocks
    private CampaignService service;

    @Test
    void findAll_returnsAllCampaigns() {
        List<Campaign> campaigns = List.of(
                Campaign.builder().id(1L).name("Summer Sale").build(),
                Campaign.builder().id(2L).name("Office Push").build()
        );
        when(repository.findAll()).thenReturn(campaigns);
        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_setsDefaultStatusWhenNotProvided() {
        CampaignDto.Request request = CampaignDto.Request.builder()
                .name("TikTok June")
                .channel("TikTok")
                .build();

        when(repository.save(any(Campaign.class))).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Campaign result = service.create(request);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void create_usesProvidedStatus() {
        CampaignDto.Request request = CampaignDto.Request.builder()
                .name("Draft Campaign")
                .channel("Shopee")
                .status("DRAFT")
                .build();

        when(repository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));
        Campaign result = service.create(request);
        assertThat(result.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        CampaignDto.Request request = CampaignDto.Request.builder().name("X").channel("Y").build();
        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_callsRepositoryDelete() {
        Campaign campaign = Campaign.builder().id(1L).name("To Delete").build();
        when(repository.findById(1L)).thenReturn(Optional.of(campaign));
        service.delete(1L);
        verify(repository).delete(campaign);
    }
}
