package com.bnpaper.agento.campaign;

import com.bnpaper.agento.common.dto.ApiResponse;
import com.bnpaper.agento.content.GeneratedContentDto;
import com.bnpaper.agento.content.GeneratedContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService service;
    private final GeneratedContentService contentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignDto.Response>>> findAll() {
        List<CampaignDto.Response> result = service.findAll().stream()
                .map(CampaignDto.Response::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(CampaignDto.Response.from(service.findById(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignDto.Response>> create(
            @RequestBody @Valid CampaignDto.Request request) {
        Campaign saved = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(CampaignDto.Response.from(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignDto.Response>> update(
            @PathVariable Long id,
            @RequestBody @Valid CampaignDto.Request request) {
        return ResponseEntity.ok(ApiResponse.ok(CampaignDto.Response.from(service.update(id, request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> generate(
            @PathVariable Long id,
            @RequestBody(required = false) GeneratedContentDto.GenerateRequest generateRequest) {
        if (generateRequest == null) {
            generateRequest = new GeneratedContentDto.GenerateRequest();
        }
        GeneratedContentDto.Response result = contentService.generateForCampaign(id, generateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }
}
