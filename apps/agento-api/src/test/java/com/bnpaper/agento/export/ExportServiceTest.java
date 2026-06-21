package com.bnpaper.agento.export;

import com.bnpaper.agento.calendar.CalendarItem;
import com.bnpaper.agento.calendar.CalendarItemRepository;
import com.bnpaper.agento.calendar.CalendarItemStatus;
import com.bnpaper.agento.calendar.ContentCalendar;
import com.bnpaper.agento.calendar.ContentCalendarRepository;
import com.bnpaper.agento.campaign.Campaign;
import com.bnpaper.agento.campaign.CampaignRepository;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.content.ContentStatus;
import com.bnpaper.agento.content.GeneratedContent;
import com.bnpaper.agento.content.GeneratedContentRepository;
import com.bnpaper.agento.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private ExportJobRepository exportJobRepo;
    @Mock private GeneratedContentRepository contentRepo;
    @Mock private CalendarItemRepository calendarItemRepo;
    @Mock private ContentCalendarRepository calendarRepo;
    @Mock private CampaignRepository campaignRepo;
    @Mock private StorageService storageService;

    @InjectMocks
    private ExportService service;

    private UUID jobId;
    private ExportJob pendingJob;
    private UUID campaignId;
    private UUID calendarId;

    @BeforeEach
    void setUp() {
        jobId = UUID.randomUUID();
        campaignId = UUID.randomUUID();
        calendarId = UUID.randomUUID();

        pendingJob = ExportJob.builder()
                .id(jobId)
                .exportType(ExportType.CONTENT_CSV)
                .status(ExportStatus.PENDING)
                .includeStatuses("APPROVED")
                .build();
    }

    // ── createContentExportJob ────────────────────────────────────────────────

    @Test
    void createContentExportJob_savesAndReturnsPendingJob() {
        ExportJobDto.ContentExportRequest req = ExportJobDto.ContentExportRequest.builder()
                .exportType(ExportType.CONTENT_CSV)
                .build();
        when(exportJobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExportJob result = service.createContentExportJob(req);

        assertThat(result.getStatus()).isEqualTo(ExportStatus.PENDING);
        assertThat(result.getExportType()).isEqualTo(ExportType.CONTENT_CSV);
        assertThat(result.getIncludeStatuses()).isEqualTo("APPROVED");
        verify(exportJobRepo).save(any());
    }

    @Test
    void createContentExportJob_usesCustomStatuses() {
        ExportJobDto.ContentExportRequest req = ExportJobDto.ContentExportRequest.builder()
                .exportType(ExportType.CONTENT_CSV)
                .statuses(List.of("APPROVED", "DRAFT"))
                .build();
        when(exportJobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExportJob result = service.createContentExportJob(req);

        assertThat(result.getIncludeStatuses()).contains("APPROVED");
        assertThat(result.getIncludeStatuses()).contains("DRAFT");
    }

    // ── createCalendarExportJob ───────────────────────────────────────────────

    @Test
    void createCalendarExportJob_throwsWhenCalendarNotFound() {
        when(calendarRepo.existsById(calendarId)).thenReturn(false);
        assertThatThrownBy(() -> service.createCalendarExportJob(calendarId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCalendarExportJob_createsJob() {
        when(calendarRepo.existsById(calendarId)).thenReturn(true);
        when(exportJobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExportJob result = service.createCalendarExportJob(calendarId);

        assertThat(result.getCalendarId()).isEqualTo(calendarId);
        assertThat(result.getExportType()).isEqualTo(ExportType.CALENDAR_CSV);
    }

    // ── runContentExport ──────────────────────────────────────────────────────

    @Test
    void runContentExport_completesWithApprovedContent() {
        GeneratedContent content = approvedContent();

        when(exportJobRepo.findById(jobId)).thenReturn(Optional.of(pendingJob));
        when(exportJobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(contentRepo.findByStatusInOrderByCreatedAtAsc(List.of(ContentStatus.APPROVED)))
                .thenReturn(List.of(content));
        when(campaignRepo.findAllById(any())).thenReturn(List.of());
        when(storageService.store(anyString(), any())).thenReturn("/tmp/export.csv");

        service.runContentExport(jobId);

        verify(exportJobRepo, atLeast(2)).save(argThat(j -> j instanceof ExportJob));
        // Last save should be COMPLETED
        verify(storageService).store(anyString(), any());
    }

    @Test
    void runContentExport_filtersOutNonMatchingChannel() {
        pendingJob.setChannel("tiktok");
        GeneratedContent tiktok = approvedContent();
        tiktok.setChannel("tiktok");
        GeneratedContent shopee = approvedContent();
        shopee.setChannel("shopee");

        when(exportJobRepo.findById(jobId)).thenReturn(Optional.of(pendingJob));
        when(exportJobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(contentRepo.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(List.of(tiktok, shopee));
        when(campaignRepo.findAllById(any())).thenReturn(List.of());
        when(storageService.store(anyString(), any())).thenAnswer(inv -> "/tmp/" + inv.getArgument(0));

        service.runContentExport(jobId);

        // Verify storage was called with CSV containing only 1 row (+ header)
        verify(storageService).store(anyString(), argThat(bytes -> {
            String csv = new String(bytes, StandardCharsets.UTF_8);
            return csv.lines().count() == 2; // header + 1 data row
        }));
    }

    @Test
    void runContentExport_marksFailedOnError() {
        when(exportJobRepo.findById(jobId)).thenReturn(Optional.of(pendingJob));
        when(exportJobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(contentRepo.findByStatusInOrderByCreatedAtAsc(any()))
                .thenThrow(new RuntimeException("DB error"));

        service.runContentExport(jobId);

        verify(exportJobRepo, atLeast(2)).save(argThat(j ->
                j instanceof ExportJob ej && ej.getStatus() == ExportStatus.FAILED));
    }

    // ── runCalendarExport ─────────────────────────────────────────────────────

    @Test
    void runCalendarExport_completesWithCalendarItems() {
        ExportJob calJob = ExportJob.builder()
                .id(jobId)
                .exportType(ExportType.CALENDAR_CSV)
                .status(ExportStatus.PENDING)
                .calendarId(calendarId)
                .includeStatuses("APPROVED")
                .build();

        ContentCalendar calendar = ContentCalendar.builder()
                .id(calendarId).name("June Cal")
                .periodStart(LocalDate.of(2026, 6, 1))
                .periodEnd(LocalDate.of(2026, 6, 30))
                .build();

        CalendarItem item = CalendarItem.builder()
                .id(UUID.randomUUID())
                .calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 1))
                .channel("tiktok")
                .status(CalendarItemStatus.PLANNED)
                .build();

        when(exportJobRepo.findById(jobId)).thenReturn(Optional.of(calJob));
        when(exportJobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(calendarRepo.findById(calendarId)).thenReturn(Optional.of(calendar));
        when(calendarItemRepo.findByCalendarIdOrderByPlannedDateAsc(calendarId)).thenReturn(List.of(item));
        when(contentRepo.findAllById(any())).thenReturn(List.of());
        when(storageService.store(anyString(), any())).thenReturn("/tmp/cal.csv");

        service.runCalendarExport(jobId);

        verify(storageService).store(anyString(), any());
    }

    // ── buildContentCsv ───────────────────────────────────────────────────────

    @Test
    void buildContentCsv_producesCorrectHeaders() throws IOException {
        byte[] csv = service.buildContentCsv(List.of(), Map.of());
        String text = new String(csv, StandardCharsets.UTF_8);
        assertThat(text).contains("Date,Channel,Campaign,Content Type,Hook,Body,CTA,Hashtags,Compliance Notes,Status");
    }

    @Test
    void buildContentCsv_producesOneRowPerContent() throws IOException {
        GeneratedContent c1 = approvedContent();
        GeneratedContent c2 = approvedContent();
        byte[] csv = service.buildContentCsv(List.of(c1, c2), Map.of());
        long lines = new String(csv, StandardCharsets.UTF_8).lines().count();
        assertThat(lines).isEqualTo(3); // header + 2 rows
    }

    @Test
    void buildContentCsv_includesHashtagsJoined() throws IOException {
        GeneratedContent c = approvedContent();
        c.setHashtags(List.of("#soclean", "#tissue"));
        byte[] csv = service.buildContentCsv(List.of(c), Map.of());
        String text = new String(csv, StandardCharsets.UTF_8);
        assertThat(text).contains("#soclean #tissue");
    }

    // ── matchesContentType via export type ───────────────────────────────────

    @Test
    void runContentExport_videoScriptPackFiltersCorrectly() {
        pendingJob.setExportType(ExportType.VIDEO_SCRIPT_PACK);
        GeneratedContent script = approvedContent();
        script.setContentType("TIKTOK_SCRIPT");
        GeneratedContent caption = approvedContent();
        caption.setContentType("TIKTOK_CAPTION");

        when(exportJobRepo.findById(jobId)).thenReturn(Optional.of(pendingJob));
        when(exportJobRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(contentRepo.findByStatusInOrderByCreatedAtAsc(any())).thenReturn(List.of(script, caption));
        when(campaignRepo.findAllById(any())).thenReturn(List.of());
        when(storageService.store(anyString(), any())).thenAnswer(inv -> "/tmp/out.csv");

        service.runContentExport(jobId);

        verify(storageService).store(anyString(), argThat(bytes -> {
            String csv = new String(bytes, StandardCharsets.UTF_8);
            return csv.lines().count() == 2; // header + script only
        }));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private GeneratedContent approvedContent() {
        return GeneratedContent.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .channel("tiktok")
                .contentType("TIKTOK_CAPTION")
                .hook("Hook text")
                .body("Body text")
                .callToAction("Buy now")
                .hashtags(List.of("#soclean"))
                .status(ContentStatus.APPROVED)
                .complianceNotes("OK")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
