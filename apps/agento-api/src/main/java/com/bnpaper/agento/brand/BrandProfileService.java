package com.bnpaper.agento.brand;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandProfileService {

    private final BrandProfileRepository repository;

    public List<BrandProfileDto.Response> findAll() {
        return repository.findAll().stream()
                .map(BrandProfileDto::toResponse)
                .collect(Collectors.toList());
    }

    public BrandProfileDto.Response findById(UUID id) {
        return repository.findById(id)
                .map(BrandProfileDto::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("BrandProfile", id));
    }

    public Optional<BrandProfile> findFirstEntity() {
        return repository.findAll().stream().findFirst();
    }

    @Transactional
    public BrandProfileDto.Response create(BrandProfileDto.Request request) {
        BrandProfile entity = BrandProfile.builder()
                .brandName(request.getBrandName())
                .slogan(request.getSlogan())
                .toneOfVoice(request.getToneOfVoice())
                .targetAudience(request.getTargetAudience())
                .keyMessages(request.getKeyMessages())
                .prohibitedClaims(request.getProhibitedClaims())
                .build();
        return BrandProfileDto.toResponse(repository.save(entity));
    }

    @Transactional
    public BrandProfileDto.Response update(UUID id, BrandProfileDto.Request request) {
        BrandProfile entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BrandProfile", id));
        entity.setBrandName(request.getBrandName());
        entity.setSlogan(request.getSlogan());
        entity.setToneOfVoice(request.getToneOfVoice());
        entity.setTargetAudience(request.getTargetAudience());
        entity.setKeyMessages(request.getKeyMessages());
        entity.setProhibitedClaims(request.getProhibitedClaims());
        return BrandProfileDto.toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("BrandProfile", id);
        }
        repository.deleteById(id);
    }
}
