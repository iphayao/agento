package com.bnpaper.agento.campaign;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignService {

    private final CampaignRepository repository;

    public List<CampaignDto.Response> findAll() {
        return repository.findAll().stream()
                .map(CampaignDto::toResponse)
                .collect(Collectors.toList());
    }

    public CampaignDto.Response findById(UUID id) {
        return repository.findById(id)
                .map(CampaignDto::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", id));
    }

    public Campaign findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", id));
    }

    @Transactional
    public CampaignDto.Response create(CampaignDto.Request request) {
        Campaign entity = Campaign.builder()
                .name(request.getName())
                .objective(request.getObjective())
                .channel(request.getChannel())
                .targetAudience(request.getTargetAudience())
                .contentAngle(request.getContentAngle())
                .status(request.getStatus() != null ? request.getStatus() : "DRAFT")
                .build();
        return CampaignDto.toResponse(repository.save(entity));
    }

    @Transactional
    public CampaignDto.Response update(UUID id, CampaignDto.Request request) {
        Campaign entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", id));
        entity.setName(request.getName());
        entity.setObjective(request.getObjective());
        entity.setChannel(request.getChannel());
        entity.setTargetAudience(request.getTargetAudience());
        entity.setContentAngle(request.getContentAngle());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        return CampaignDto.toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Campaign", id);
        }
        repository.deleteById(id);
    }
}
