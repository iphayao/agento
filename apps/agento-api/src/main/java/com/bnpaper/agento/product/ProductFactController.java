package com.bnpaper.agento.product;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductFactController {

    private final ProductFactService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductFactDto.Response>>> findAll() {
        List<ProductFactDto.Response> result = service.findAll().stream()
                .map(ProductFactDto.Response::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductFactDto.Response>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ProductFactDto.Response.from(service.findById(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductFactDto.Response>> create(
            @RequestBody @Valid ProductFactDto.Request request) {
        ProductFact saved = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(ProductFactDto.Response.from(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductFactDto.Response>> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductFactDto.Request request) {
        return ResponseEntity.ok(ApiResponse.ok(ProductFactDto.Response.from(service.update(id, request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
