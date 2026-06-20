package com.bnpaper.agento.brand;

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
class BrandProfileServiceTest {

    @Mock
    private BrandProfileRepository repository;

    @InjectMocks
    private BrandProfileService service;

    private BrandProfile sampleBrand;
    private UUID brandId;

    @BeforeEach
    void setUp() {
        brandId = UUID.randomUUID();
        sampleBrand = BrandProfile.builder()
                .id(brandId)
                .brandName("SoClean")
                .slogan("สะอาด เนียนนุ่ม ฝุ่นน้อย")
                .toneOfVoice("Warm, honest, practical")
                .targetAudience("Women Gen Y, households, office buyers")
                .keyMessages(List.of("ฝุ่นน้อย", "เนียนนุ่ม", "คุ้มค่า"))
                .prohibitedClaims(List.of())
                .build();
    }

    @Test
    void findAll_returnsMappedList() {
        when(repository.findAll()).thenReturn(List.of(sampleBrand));

        List<BrandProfileDto.Response> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrandName()).isEqualTo("SoClean");
    }

    @Test
    void findById_returnsDto() {
        when(repository.findById(brandId)).thenReturn(Optional.of(sampleBrand));

        BrandProfileDto.Response result = service.findById(brandId);

        assertThat(result.getId()).isEqualTo(brandId);
        assertThat(result.getBrandName()).isEqualTo("SoClean");
        assertThat(result.getKeyMessages()).contains("ฝุ่นน้อย");
    }

    @Test
    void findById_throwsWhenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(repository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(missingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    void create_savesAndReturnsDto() {
        BrandProfileDto.Request request = new BrandProfileDto.Request();
        request.setBrandName("SoClean");
        request.setSlogan("สะอาด เนียนนุ่ม ฝุ่นน้อย");
        request.setKeyMessages(List.of("ฝุ่นน้อย", "เนียนนุ่ม"));

        when(repository.save(any(BrandProfile.class))).thenReturn(sampleBrand);

        BrandProfileDto.Response result = service.create(request);

        assertThat(result.getBrandName()).isEqualTo("SoClean");
        verify(repository, times(1)).save(any(BrandProfile.class));
    }

    @Test
    void update_modifiesExistingBrand() {
        BrandProfileDto.Request request = new BrandProfileDto.Request();
        request.setBrandName("SoClean Updated");
        request.setSlogan("New slogan");
        request.setKeyMessages(List.of("คุ้มค่า"));

        BrandProfile updated = BrandProfile.builder()
                .id(brandId)
                .brandName("SoClean Updated")
                .slogan("New slogan")
                .keyMessages(List.of("คุ้มค่า"))
                .prohibitedClaims(List.of())
                .build();

        when(repository.findById(brandId)).thenReturn(Optional.of(sampleBrand));
        when(repository.save(any(BrandProfile.class))).thenReturn(updated);

        BrandProfileDto.Response result = service.update(brandId, request);

        assertThat(result.getBrandName()).isEqualTo("SoClean Updated");
    }

    @Test
    void delete_callsRepositoryDelete() {
        when(repository.existsById(brandId)).thenReturn(true);

        service.delete(brandId);

        verify(repository, times(1)).deleteById(brandId);
    }

    @Test
    void delete_throwsWhenNotFound() {
        UUID missingId = UUID.randomUUID();
        when(repository.existsById(missingId)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findFirstEntity_returnsOptional() {
        when(repository.findAll()).thenReturn(List.of(sampleBrand));

        Optional<BrandProfile> result = service.findFirstEntity();

        assertThat(result).isPresent();
        assertThat(result.get().getBrandName()).isEqualTo("SoClean");
    }
}
