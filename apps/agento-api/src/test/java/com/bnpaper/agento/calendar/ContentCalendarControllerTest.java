package com.bnpaper.agento.calendar;

import com.bnpaper.agento.common.dto.ApiResponse;
import com.bnpaper.agento.common.exception.GlobalExceptionHandler;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
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

import java.time.LocalDate;
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
class ContentCalendarControllerTest {

    @Mock private ContentCalendarService calendarService;
    @Mock private BatchGenerationService batchService;
    @InjectMocks private ContentCalendarController controller;

    private MockMvc mvc;
    private ObjectMapper mapper = new ObjectMapper();
    private UUID calendarId;
    private ContentCalendarDto.CalendarResponse sampleCalendar;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mapper.findAndRegisterModules();

        calendarId = UUID.randomUUID();
        sampleCalendar = ContentCalendarDto.CalendarResponse.builder()
                .id(calendarId)
                .name("June 2026 Calendar")
                .periodStart(LocalDate.of(2026, 6, 1))
                .periodEnd(LocalDate.of(2026, 6, 30))
                .status("DRAFT")
                .itemCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listCalendars_returnsOk() throws Exception {
        when(calendarService.listCalendars()).thenReturn(List.of(sampleCalendar));

        mvc.perform(get("/content-calendars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("June 2026 Calendar")));
    }

    @Test
    void createCalendar_returnsCreated() throws Exception {
        when(calendarService.createCalendar(any())).thenReturn(sampleCalendar);

        String body = """
                {"name":"June 2026 Calendar","periodStart":"2026-06-01","periodEnd":"2026-06-30",
                 "objective":"Drive TikTok sales"}""";

        mvc.perform(post("/content-calendars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("June 2026 Calendar")))
                .andExpect(jsonPath("$.data.status", is("DRAFT")));
    }

    @Test
    void createCalendar_returnsBadRequestOnMissingName() throws Exception {
        mvc.perform(post("/content-calendars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"periodStart\":\"2026-06-01\",\"periodEnd\":\"2026-06-30\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findCalendar_returnsNotFoundForMissingCalendar() throws Exception {
        when(calendarService.findCalendar(calendarId))
                .thenThrow(new ResourceNotFoundException("ContentCalendar", calendarId));

        mvc.perform(get("/content-calendars/" + calendarId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCalendar_returnsOk() throws Exception {
        ContentCalendarDto.CalendarResponse updated = ContentCalendarDto.CalendarResponse.builder()
                .id(calendarId).name("Updated Name").status("APPROVED")
                .periodStart(LocalDate.of(2026, 6, 1)).periodEnd(LocalDate.of(2026, 6, 30))
                .itemCount(3).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(calendarService.updateCalendar(eq(calendarId), any())).thenReturn(updated);

        mvc.perform(put("/content-calendars/" + calendarId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\",\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name", is("Updated Name")));
    }

    @Test
    void deleteCalendar_returnsOk() throws Exception {
        doNothing().when(calendarService).deleteCalendar(calendarId);

        mvc.perform(delete("/content-calendars/" + calendarId))
                .andExpect(status().isOk());
    }

    @Test
    void listItems_returnsItems() throws Exception {
        ContentCalendarDto.ItemResponse item = ContentCalendarDto.ItemResponse.builder()
                .id(UUID.randomUUID()).calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 10)).channel("tiktok")
                .status("PLANNED").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(calendarService.listItems(calendarId)).thenReturn(List.of(item));

        mvc.perform(get("/content-calendars/" + calendarId + "/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].channel", is("tiktok")));
    }

    @Test
    void planCalendar_returnsOk() throws Exception {
        ContentCalendarDto.ItemResponse item = ContentCalendarDto.ItemResponse.builder()
                .id(UUID.randomUUID()).calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 10)).channel("tiktok")
                .contentAngle("ฝุ่นน้อย").status("PLANNED")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(calendarService.planCalendar(calendarId)).thenReturn(List.of(item));

        mvc.perform(post("/content-calendars/" + calendarId + "/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void startBatch_returnsAccepted() throws Exception {
        ContentCalendarDto.BatchJobResponse job = ContentCalendarDto.BatchJobResponse.builder()
                .id(UUID.randomUUID()).calendarId(calendarId).status("RUNNING")
                .totalItems(7).completedItems(0).failedItems(0)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(batchService.startBatch(calendarId)).thenReturn(job);

        mvc.perform(post("/content-calendars/" + calendarId + "/generate"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status", is("RUNNING")))
                .andExpect(jsonPath("$.data.totalItems", is(7)));
    }

    @Test
    void getLatestJob_returnsOk() throws Exception {
        ContentCalendarDto.BatchJobResponse job = ContentCalendarDto.BatchJobResponse.builder()
                .id(UUID.randomUUID()).calendarId(calendarId).status("COMPLETED")
                .totalItems(7).completedItems(7).failedItems(0)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(batchService.getLatestJob(calendarId)).thenReturn(job);

        mvc.perform(get("/content-calendars/" + calendarId + "/batch-job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.completedItems", is(7)));
    }
}
