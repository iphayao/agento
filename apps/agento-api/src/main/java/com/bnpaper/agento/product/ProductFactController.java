package com.bnpaper.agento.product;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductFactController {

    private final ProductFactService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductFactDto.Response>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductFactDto.Response>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductFactDto.Response>> create(
            @Valid @RequestBody ProductFactDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Product fact created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductFactDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductFactDto.Request request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request), "Product fact updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product fact deleted"));
    }
}
