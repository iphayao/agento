package com.bnpaper.agento.export;

import com.bnpaper.agento.common.exception.GlobalExceptionHandler;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.storage.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

    @Mock private ExportService exportService;
    @Mock private StorageService storageService;
    @Mock private com.bnpaper.agento.audit.AuditService auditService;

    @InjectMocks private ExportController controller;

    private MockMvc mvc;
    private ObjectMapper mapper;
    private UUID jobId;
    private ExportJob sampleJob;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        jobId = UUID.randomUUID();
        sampleJob = ExportJob.builder()
                .id(jobId)
                .exportType(ExportType.CONTENT_CSV)
                .status(ExportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── POST /exports/content ─────────────────────────────────────────────────

    @Test
    void postContent_returns202AndPendingJob() throws Exception {
        when(exportService.createContentExportJob(any())).thenReturn(sampleJob);
        doNothing().when(exportService).runContentExport(jobId);

        String body = """
                {"exportType":"CONTENT_CSV","statuses":["APPROVED"]}
                """;
        mvc.perform(post("/exports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void postContent_returns400WithoutExportType() throws Exception {
        mvc.perform(post("/exports/content")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /exports/calendar/{id} ───────────────────────────────────────────

    @Test
    void postCalendar_returns202() throws Exception {
        UUID calId = UUID.randomUUID();
        ExportJob calJob = ExportJob.builder()
                .id(jobId)
                .exportType(ExportType.CALENDAR_CSV)
                .status(ExportStatus.PENDING)
                .calendarId(calId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(exportService.createCalendarExportJob(calId)).thenReturn(calJob);
        doNothing().when(exportService).runCalendarExport(jobId);

        mvc.perform(post("/exports/calendar/" + calId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.exportType").value("CALENDAR_CSV"));
    }

    @Test
    void postCalendar_returns404WhenCalendarMissing() throws Exception {
        UUID calId = UUID.randomUUID();
        when(exportService.createCalendarExportJob(calId))
                .thenThrow(new ResourceNotFoundException("ContentCalendar", calId));

        mvc.perform(post("/exports/calendar/" + calId))
                .andExpect(status().isNotFound());
    }

    // ── GET /exports ──────────────────────────────────────────────────────────

    @Test
    void list_returnsAllJobs() throws Exception {
        when(exportService.listJobs()).thenReturn(List.of(sampleJob));

        mvc.perform(get("/exports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].exportType").value("CONTENT_CSV"));
    }

    // ── GET /exports/{id} ─────────────────────────────────────────────────────

    @Test
    void getById_returnsJob() throws Exception {
        when(exportService.getJob(jobId)).thenReturn(sampleJob);

        mvc.perform(get("/exports/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(jobId.toString()));
    }

    @Test
    void getById_returns404WhenMissing() throws Exception {
        when(exportService.getJob(jobId)).thenThrow(new ResourceNotFoundException("ExportJob", jobId));

        mvc.perform(get("/exports/" + jobId))
                .andExpect(status().isNotFound());
    }

    // ── GET /exports/{id}/download ────────────────────────────────────────────

    @Test
    void download_returnsFileBytes() throws Exception {
        ExportJob completedJob = ExportJob.builder()
                .id(jobId)
                .exportType(ExportType.CONTENT_CSV)
                .status(ExportStatus.COMPLETED)
                .fileUrl("/tmp/test.csv")
                .fileName("test.csv")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(exportService.getJob(jobId)).thenReturn(completedJob);
        when(storageService.retrieve("/tmp/test.csv")).thenReturn("date,channel\n".getBytes());

        mvc.perform(get("/exports/" + jobId + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Type", containsString("text/csv")));
    }

    @Test
    void download_returns400WhenNotCompleted() throws Exception {
        when(exportService.getJob(jobId)).thenReturn(sampleJob); // PENDING

        mvc.perform(get("/exports/" + jobId + "/download"))
                .andExpect(status().isBadRequest());
    }
}
