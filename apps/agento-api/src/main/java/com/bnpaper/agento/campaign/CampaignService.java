package com.bnpaper.agento.campaign;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository repository;

    @Transactional(readOnly = true)
    public List<Campaign> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Campaign findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", id));
    }

    @Transactional
    public Campaign create(CampaignDto.Request request) {
        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .objective(request.getObjective())
                .channel(request.getChannel())
                .targetAudience(request.getTargetAudience())
                .contentAngle(request.getContentAngle())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();
        return repository.save(campaign);
    }

    @Transactional
    public Campaign update(Long id, CampaignDto.Request request) {
        Campaign campaign = findById(id);
        campaign.setName(request.getName());
        campaign.setObjective(request.getObjective());
        campaign.setChannel(request.getChannel());
        campaign.setTargetAudience(request.getTargetAudience());
        campaign.setContentAngle(request.getContentAngle());
        if (request.getStatus() != null) {
            campaign.setStatus(request.getStatus());
        }
        return repository.save(campaign);
    }

    @Transactional
    public void delete(Long id) {
        Campaign campaign = findById(id);
        repository.delete(campaign);
    }
}
