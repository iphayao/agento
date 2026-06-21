package com.bnpaper.agento.export;

import com.bnpaper.agento.common.dto.ApiResponse;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.storage.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private final StorageService storageService;

    @PostMapping("/content")
    public ResponseEntity<ApiResponse<ExportJobDto.ExportJobResponse>> exportContent(
            @Valid @RequestBody ExportJobDto.ContentExportRequest req,
            HttpServletRequest httpReq) {

        ExportJob job = exportService.createContentExportJob(req);
        exportService.runContentExport(job.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(toResponse(job, httpReq), "Export started"));
    }

    @PostMapping("/calendar/{calendarId}")
    public ResponseEntity<ApiResponse<ExportJobDto.ExportJobResponse>> exportCalendar(
            @PathVariable UUID calendarId,
            HttpServletRequest httpReq) {

        ExportJob job = exportService.createCalendarExportJob(calendarId);
        exportService.runCalendarExport(job.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(toResponse(job, httpReq), "Export started"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExportJobDto.ExportJobResponse>>> list(HttpServletRequest httpReq) {
        List<ExportJobDto.ExportJobResponse> jobs = exportService.listJobs().stream()
                .map(j -> toResponse(j, httpReq))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExportJobDto.ExportJobResponse>> get(
            @PathVariable UUID id,
            HttpServletRequest httpReq) {

        ExportJob job = exportService.getJob(id);
        return ResponseEntity.ok(ApiResponse.success(toResponse(job, httpReq)));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        ExportJob job = exportService.getJob(id);
        if (job.getStatus() != ExportStatus.COMPLETED || job.getFileUrl() == null) {
            throw new IllegalStateException("Export is not ready for download");
        }

        byte[] data = storageService.retrieve(job.getFileUrl());
        String filename = job.getFileName() != null ? job.getFileName() : "export.csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ExportJobDto.ExportJobResponse toResponse(ExportJob job, HttpServletRequest req) {
        String downloadUrl = job.getStatus() == ExportStatus.COMPLETED
                ? buildDownloadUrl(req, job.getId())
                : null;
        return ExportJobDto.toResponse(job, downloadUrl);
    }

    private String buildDownloadUrl(HttpServletRequest req, UUID jobId) {
        String base = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath();
        return base + "/exports/" + jobId + "/download";
    }
}
