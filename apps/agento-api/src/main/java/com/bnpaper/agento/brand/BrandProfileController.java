package com.bnpaper.agento.brand;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brand")
@RequiredArgsConstructor
public class BrandProfileController {

    private final BrandProfileService service;

    @GetMapping
    public ResponseEntity<ApiResponse<BrandProfileDto.Response>> get() {
        return service.findLatest()
                .map(p -> ResponseEntity.ok(ApiResponse.ok(BrandProfileDto.Response.from(p))))
                .orElse(ResponseEntity.ok(ApiResponse.ok(null)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandProfileDto.Response>> create(
            @RequestBody @Valid BrandProfileDto.Request request) {
        BrandProfile saved = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(BrandProfileDto.Response.from(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandProfileDto.Response>> update(
            @PathVariable Long id,
            @RequestBody @Valid BrandProfileDto.Request request) {
        BrandProfile updated = service.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(BrandProfileDto.Response.from(updated)));
    }
}
