package com.bnpaper.agento.knowledge;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeDocumentService service;

    @GetMapping
    public ApiResponse<List<KnowledgeDocumentDto.Response>> findAll() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeDocumentDto.Response> findById(@PathVariable UUID id) {
        return ApiResponse.success(service.findById(id));
    }

    @GetMapping("/{id}/chunks")
    public ApiResponse<List<KnowledgeDocumentDto.ChunkResponse>> findChunks(@PathVariable UUID id) {
        return ApiResponse.success(service.findChunks(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KnowledgeDocumentDto.Response> create(
            @Valid @RequestBody KnowledgeDocumentDto.Request request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeDocumentDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody KnowledgeDocumentDto.Request request) {
        return ApiResponse.success(service.update(id, request));
    }

    @PutMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable UUID id) {
        service.archive(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/search")
    public ApiResponse<KnowledgeSearchResult> search(
            @Valid @RequestBody KnowledgeSearchRequest request) {
        return ApiResponse.success(service.search(request));
    }
}
