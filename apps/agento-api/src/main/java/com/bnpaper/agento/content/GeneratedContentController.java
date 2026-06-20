package com.bnpaper.agento.content;

import com.bnpaper.agento.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class GeneratedContentController {

    private final GeneratedContentService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GeneratedContentDto.Response>>> findAll(
            @RequestParam(required = false) Long campaignId) {
        List<GeneratedContentDto.Response> result = campaignId != null
                ? service.findByCampaignId(campaignId)
                : service.findAll();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.approve(id)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.reject(id)));
    }
}
