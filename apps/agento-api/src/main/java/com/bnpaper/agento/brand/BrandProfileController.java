package com.bnpaper.agento.brand;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandProfileController {

    private final BrandProfileService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandProfileDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandProfileDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandProfileDto.Response>> create(
            @Valid @RequestBody BrandProfileDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Brand profile created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandProfileDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BrandProfileDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request), "Brand profile updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Brand profile deleted"));
    }
}
