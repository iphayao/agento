package com.bnpaper.agento.content;

import com.bnpaper.agento.audit.AuditAction;
import com.bnpaper.agento.audit.AuditService;
import com.bnpaper.agento.common.dto.ApiResponse;
import com.bnpaper.agento.common.ratelimit.AiRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GeneratedContentController {

    private final GeneratedContentService service;
    private final AuditService auditService;
    private final AiRateLimiter rateLimiter;

    @PostMapping("/campaigns/{campaignId}/content/generate")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> generate(
            @PathVariable UUID campaignId,
            @Valid @RequestBody GeneratedContentDto.GenerateRequest request,
            Authentication auth,
            HttpServletRequest httpReq) {

        rateLimiter.consume(resolveKey(auth, httpReq));

        GeneratedContentDto.Response response = service.generate(campaignId, request);

        auditService.log(AuditAction.CONTENT_GENERATED, "GeneratedContent", response.getId(),
                "campaign=" + campaignId + " model=" + response.getAiModel(), httpReq.getRemoteAddr());

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
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> approve(
            @PathVariable UUID id, HttpServletRequest httpReq) {
        GeneratedContentDto.Response res = service.approve(id);
        auditService.log(AuditAction.CONTENT_APPROVED, "GeneratedContent", id, null, httpReq.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(res, "Content approved"));
    }

    @PutMapping("/content/{id}/reject")
    public ResponseEntity<ApiResponse<GeneratedContentDto.Response>> reject(
            @PathVariable UUID id, HttpServletRequest httpReq) {
        GeneratedContentDto.Response res = service.reject(id);
        auditService.log(AuditAction.CONTENT_REJECTED, "GeneratedContent", id, null, httpReq.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(res, "Content rejected"));
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id, HttpServletRequest httpReq) {
        service.delete(id);
        auditService.log(AuditAction.CONTENT_DELETED, "GeneratedContent", id, null, httpReq.getRemoteAddr());
        return ResponseEntity.ok(ApiResponse.success(null, "Content deleted"));
    }

    private String resolveKey(Authentication auth, HttpServletRequest req) {
        return auth != null ? auth.getName() : req.getRemoteAddr();
    }
}
