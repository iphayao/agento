package com.bnpaper.agento.export;

import com.bnpaper.agento.calendar.CalendarItem;
import com.bnpaper.agento.calendar.CalendarItemRepository;
import com.bnpaper.agento.calendar.ContentCalendar;
import com.bnpaper.agento.calendar.ContentCalendarRepository;
import com.bnpaper.agento.campaign.Campaign;
import com.bnpaper.agento.campaign.CampaignRepository;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.content.ContentStatus;
import com.bnpaper.agento.content.GeneratedContent;
import com.bnpaper.agento.content.GeneratedContentRepository;
import com.bnpaper.agento.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExportJobRepository exportJobRepo;
    private final GeneratedContentRepository contentRepo;
    private final CalendarItemRepository calendarItemRepo;
    private final ContentCalendarRepository calendarRepo;
    private final CampaignRepository campaignRepo;
    private final StorageService storageService;

    // ── Public API ─────────────────────────────────────────────────────────────

    @Transactional
    public ExportJob createContentExportJob(ExportJobDto.ContentExportRequest req) {
        List<String> statuses = req.getStatuses() != null && !req.getStatuses().isEmpty()
                ? req.getStatuses()
                : List.of("APPROVED");
        ExportJob job = ExportJob.builder()
                .exportType(req.getExportType())
                .status(ExportStatus.PENDING)
                .campaignId(req.getCampaignId())
                .channel(req.getChannel())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .includeStatuses(String.join(",", statuses))
                .build();
        return exportJobRepo.save(job);
    }

    @Transactional
    public ExportJob createCalendarExportJob(UUID calendarId) {
        if (!calendarRepo.existsById(calendarId)) {
            throw new ResourceNotFoundException("ContentCalendar", calendarId);
        }
        ExportJob job = ExportJob.builder()
                .exportType(ExportType.CALENDAR_CSV)
                .status(ExportStatus.PENDING)
                .calendarId(calendarId)
                .includeStatuses("APPROVED")
                .build();
        return exportJobRepo.save(job);
    }

    @Transactional(readOnly = true)
    public ExportJob getJob(UUID id) {
        return exportJobRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", id));
    }

    @Transactional(readOnly = true)
    public List<ExportJob> listJobs() {
        return exportJobRepo.findAllByOrderByCreatedAtDesc();
    }

    // ── Async Generation ───────────────────────────────────────────────────────

    @Async("exportExecutor")
    @Transactional
    public void runContentExport(UUID jobId) {
        ExportJob job = exportJobRepo.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", jobId));

        markRunning(job);

        try {
            List<ContentStatus> statuses = parseStatuses(job.getIncludeStatuses());
            List<GeneratedContent> allContent = contentRepo.findByStatusInOrderByCreatedAtAsc(statuses);

            // Apply optional filters in memory
            List<GeneratedContent> filtered = allContent.stream()
                    .filter(c -> job.getCampaignId() == null || job.getCampaignId().equals(c.getCampaignId()))
                    .filter(c -> job.getChannel() == null || job.getChannel().equalsIgnoreCase(c.getChannel()))
                    .filter(c -> job.getStartDate() == null || !c.getCreatedAt().toLocalDate().isBefore(job.getStartDate()))
                    .filter(c -> job.getEndDate() == null || !c.getCreatedAt().toLocalDate().isAfter(job.getEndDate()))
                    .filter(c -> matchesContentType(job.getExportType(), c.getContentType()))
                    .toList();

            // Batch-load campaign names
            Map<UUID, String> campaignNames = loadCampaignNames(filtered);

            byte[] csv = buildContentCsv(filtered, campaignNames);

            String filename = buildFilename(job);
            String fileUrl = storageService.store(filename, csv);

            markCompleted(job, fileUrl, filename, filtered.size());
            log.info("Export job {} completed: {} rows, file={}", jobId, filtered.size(), filename);
        } catch (Exception e) {
            log.error("Export job {} failed", jobId, e);
            markFailed(job, e.getMessage());
        }
    }

    @Async("exportExecutor")
    @Transactional
    public void runCalendarExport(UUID jobId) {
        ExportJob job = exportJobRepo.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", jobId));

        markRunning(job);

        try {
            UUID calendarId = job.getCalendarId();
            ContentCalendar calendar = calendarRepo.findById(calendarId)
                    .orElseThrow(() -> new ResourceNotFoundException("ContentCalendar", calendarId));

            List<CalendarItem> items = calendarItemRepo.findByCalendarIdOrderByPlannedDateAsc(calendarId);

            // Load generated content for completed items
            List<UUID> contentIds = items.stream()
                    .map(CalendarItem::getGeneratedContentId)
                    .filter(Objects::nonNull)
                    .toList();
            Map<UUID, GeneratedContent> contentMap = contentRepo.findAllById(contentIds).stream()
                    .collect(Collectors.toMap(GeneratedContent::getId, c -> c));

            byte[] csv = buildCalendarCsv(calendar, items, contentMap);

            String filename = buildFilename(job);
            String fileUrl = storageService.store(filename, csv);

            markCompleted(job, fileUrl, filename, items.size());
            log.info("Calendar export job {} completed: {} items", jobId, items.size());
        } catch (Exception e) {
            log.error("Calendar export job {} failed", jobId, e);
            markFailed(job, e.getMessage());
        }
    }

    // ── CSV Builders ───────────────────────────────────────────────────────────

    byte[] buildContentCsv(List<GeneratedContent> content, Map<UUID, String> campaignNames) throws IOException {
        String[] headers = {
                "Date", "Channel", "Campaign", "Content Type",
                "Hook", "Body", "CTA", "Hashtags", "Compliance Notes", "Status"
        };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(headers)
                     .build())) {

            for (GeneratedContent c : content) {
                printer.printRecord(
                        c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate().toString() : "",
                        nvl(c.getChannel()),
                        c.getCampaignId() != null ? campaignNames.getOrDefault(c.getCampaignId(), "") : "",
                        nvl(c.getContentType()),
                        nvl(c.getHook()),
                        nvl(c.getBody()),
                        nvl(c.getCallToAction()),
                        c.getHashtags() != null ? String.join(" ", c.getHashtags()) : "",
                        nvl(c.getComplianceNotes()),
                        c.getStatus() != null ? c.getStatus().name() : ""
                );
            }
        }
        return out.toByteArray();
    }

    byte[] buildCalendarCsv(ContentCalendar calendar, List<CalendarItem> items,
                             Map<UUID, GeneratedContent> contentMap) throws IOException {
        String[] headers = {
                "Date", "Channel", "Content Type", "Content Angle",
                "Hook Direction", "CTA Direction", "Item Status",
                "Generated Hook", "Generated Body", "Generated CTA",
                "Generated Hashtags", "Compliance Notes", "Content Status"
        };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(headers)
                     .build())) {

            for (CalendarItem item : items) {
                GeneratedContent gc = item.getGeneratedContentId() != null
                        ? contentMap.get(item.getGeneratedContentId())
                        : null;
                printer.printRecord(
                        item.getPlannedDate() != null ? item.getPlannedDate().toString() : "",
                        nvl(item.getChannel()),
                        nvl(item.getContentType()),
                        nvl(item.getContentAngle()),
                        nvl(item.getHookDirection()),
                        nvl(item.getCtaDirection()),
                        item.getStatus() != null ? item.getStatus().name() : "",
                        gc != null ? nvl(gc.getHook()) : "",
                        gc != null ? nvl(gc.getBody()) : "",
                        gc != null ? nvl(gc.getCallToAction()) : "",
                        gc != null && gc.getHashtags() != null ? String.join(" ", gc.getHashtags()) : "",
                        gc != null ? nvl(gc.getComplianceNotes()) : "",
                        gc != null && gc.getStatus() != null ? gc.getStatus().name() : ""
                );
            }
        }
        return out.toByteArray();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void markRunning(ExportJob job) {
        job.setStatus(ExportStatus.RUNNING);
        exportJobRepo.save(job);
    }

    private void markCompleted(ExportJob job, String fileUrl, String fileName, int rowCount) {
        job.setStatus(ExportStatus.COMPLETED);
        job.setFileUrl(fileUrl);
        job.setFileName(fileName);
        job.setRowCount(rowCount);
        exportJobRepo.save(job);
    }

    private void markFailed(ExportJob job, String error) {
        job.setStatus(ExportStatus.FAILED);
        job.setErrorMessage(error);
        exportJobRepo.save(job);
    }

    private List<ContentStatus> parseStatuses(String csv) {
        if (csv == null || csv.isBlank()) return List.of(ContentStatus.APPROVED);
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(s -> {
                    try { return ContentStatus.valueOf(s); }
                    catch (IllegalArgumentException e) { return null; }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean matchesContentType(ExportType exportType, String contentType) {
        return switch (exportType) {
            case VIDEO_SCRIPT_PACK -> contentType != null && contentType.toUpperCase().contains("SCRIPT");
            case RESELLER_CAPTION_PACK -> "RESELLER_POST".equalsIgnoreCase(contentType);
            case MARKETPLACE_COPY_PACK -> contentType != null &&
                    (contentType.toUpperCase().contains("SHOPEE") || contentType.toUpperCase().contains("LAZADA"));
            default -> true;
        };
    }

    private Map<UUID, String> loadCampaignNames(List<GeneratedContent> content) {
        Set<UUID> ids = content.stream()
                .map(GeneratedContent::getCampaignId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) return Map.of();
        return campaignRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Campaign::getId, Campaign::getName));
    }

    private String buildFilename(ExportJob job) {
        String date = LocalDate.now().toString();
        String type = job.getExportType().name().toLowerCase();
        return job.getId() + "_" + type + "_" + date + ".csv";
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}
