package com.bnpaper.agento.campaign;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CampaignDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CampaignDto.Response>> create(
            @Valid @RequestBody CampaignDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Campaign created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CampaignDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request), "Campaign updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Campaign deleted"));
    }
}
