package com.bnpaper.agento.brand;

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
class BrandProfileServiceTest {

    @Mock
    private BrandProfileRepository repository;

    @InjectMocks
    private BrandProfileService service;

    @Test
    void findLatest_returnsEmptyWhenNoProfile() {
        when(repository.findTopByOrderByIdAsc()).thenReturn(Optional.empty());
        assertThat(service.findLatest()).isEmpty();
    }

    @Test
    void findLatest_returnsProfileWhenExists() {
        BrandProfile profile = BrandProfile.builder().id(1L).brandName("SoClean").build();
        when(repository.findTopByOrderByIdAsc()).thenReturn(Optional.of(profile));
        assertThat(service.findLatest()).isPresent().hasValue(profile);
    }

    @Test
    void create_savesProfileWithCorrectFields() {
        BrandProfileDto.Request request = BrandProfileDto.Request.builder()
                .brandName("SoClean")
                .slogan("สะอาด เนียนนุ่ม ฝุ่นน้อย")
                .toneOfVoice("Warm, honest, practical")
                .keyMessages(List.of("เนียนนุ่ม", "ฝุ่นน้อย"))
                .build();

        BrandProfile saved = BrandProfile.builder().id(1L).brandName("SoClean").build();
        when(repository.save(any(BrandProfile.class))).thenReturn(saved);

        BrandProfile result = service.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).save(any(BrandProfile.class));
    }

    @Test
    void update_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        BrandProfileDto.Request request = BrandProfileDto.Request.builder().brandName("SoClean").build();

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_updatesFieldsAndSaves() {
        BrandProfile existing = BrandProfile.builder().id(1L).brandName("Old").build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BrandProfileDto.Request request = BrandProfileDto.Request.builder()
                .brandName("SoClean Updated")
                .slogan("New slogan")
                .build();

        BrandProfile result = service.update(1L, request);
        assertThat(result.getBrandName()).isEqualTo("SoClean Updated");
        assertThat(result.getSlogan()).isEqualTo("New slogan");
    }
}
