package com.bnpaper.agento.content;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GeneratedContentController {

    private final GeneratedContentService service;

    @PostMapping("/campaigns/{campaignId}/content/generate")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> generate(
            @PathVariable UUID campaignId,
            @Valid @RequestBody GeneratedContentDto.GenerateRequest request) {
        GeneratedContentDto.Response response = service.generate(campaignId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Content generated as DRAFT"));
    }

    @GetMapping("/campaigns/{campaignId}/content")
    public ResponseEntity<ApiResponse<List<GeneratedContentDto.Response>>> findByCampaign(
            @PathVariable UUID campaignId) {
        return ResponseEntity.ok(ApiResponse.success(service.findByCampaignId(campaignId)));
    }

    @GetMapping("/content")
    public ResponseEntity<ApiResponse<List<GeneratedContentDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(service.findAll()));
    }

    @GetMapping("/content/{id}")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PutMapping("/content/{id}/approve")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id), "Content approved"));
    }

    @PutMapping("/content/{id}/reject")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(id), "Content rejected"));
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Content deleted"));
    }
}
