package com.bnpaper.agento.knowledge;

import com.bnpaper.agento.audit.AuditAction;
import com.bnpaper.agento.audit.AuditService;
import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AuditService auditService;

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
            @Valid @RequestBody KnowledgeDocumentDto.Request request,
            HttpServletRequest httpReq) {
        KnowledgeDocumentDto.Response res = service.create(request);
        auditService.log(AuditAction.KNOWLEDGE_CREATED, "KnowledgeDocument", res.getId(),
                "title=" + request.getTitle(), httpReq.getRemoteAddr());
        return ApiResponse.success(res);
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeDocumentDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody KnowledgeDocumentDto.Request request,
            HttpServletRequest httpReq) {
        KnowledgeDocumentDto.Response res = service.update(id, request);
        auditService.log(AuditAction.KNOWLEDGE_UPDATED, "KnowledgeDocument", id,
                "title=" + request.getTitle(), httpReq.getRemoteAddr());
        return ApiResponse.success(res);
    }

    @PutMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable UUID id, HttpServletRequest httpReq) {
        service.archive(id);
        auditService.log(AuditAction.KNOWLEDGE_ARCHIVED, "KnowledgeDocument", id, null, httpReq.getRemoteAddr());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id, HttpServletRequest httpReq) {
        service.delete(id);
        auditService.log(AuditAction.KNOWLEDGE_DELETED, "KnowledgeDocument", id, null, httpReq.getRemoteAddr());
        return ApiResponse.success(null);
    }

    @PostMapping("/search")
    public ApiResponse<KnowledgeSearchResult> search(
            @Valid @RequestBody KnowledgeSearchRequest request) {
        return ApiResponse.success(service.search(request));
    }
}
