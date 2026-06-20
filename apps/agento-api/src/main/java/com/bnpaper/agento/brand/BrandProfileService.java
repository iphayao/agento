package com.bnpaper.agento.brand;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandProfileService {

    private final BrandProfileRepository repository;

    @Transactional(readOnly = true)
    public Optional<BrandProfile> findLatest() {
        return repository.findTopByOrderByIdAsc();
    }

    @Transactional
    public BrandProfile create(BrandProfileDto.Request request) {
        BrandProfile profile = BrandProfile.builder()
                .brandName(request.getBrandName())
                .slogan(request.getSlogan())
                .toneOfVoice(request.getToneOfVoice())
                .targetAudience(request.getTargetAudience())
                .keyMessages(request.getKeyMessages())
                .prohibitedClaims(request.getProhibitedClaims())
                .build();
        return repository.save(profile);
    }

    @Transactional
    public BrandProfile update(Long id, BrandProfileDto.Request request) {
        BrandProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BrandProfile", id));
        profile.setBrandName(request.getBrandName());
        profile.setSlogan(request.getSlogan());
        profile.setToneOfVoice(request.getToneOfVoice());
        profile.setTargetAudience(request.getTargetAudience());
        profile.setKeyMessages(request.getKeyMessages());
        profile.setProhibitedClaims(request.getProhibitedClaims());
        return repository.save(profile);
    }
}
