package com.bnpaper.agento.calendar;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.workflow.AgentWorkflow;
import com.bnpaper.agento.workflow.AgentWorkflowService;
import com.bnpaper.agento.workflow.AgentWorkflowStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchGenerationServiceTest {

    @Mock private BatchGenerationJobRepository batchJobRepo;
    @Mock private CalendarItemRepository itemRepo;
    @Mock private ContentCalendarRepository calendarRepo;
    @Mock private AgentWorkflowService workflowService;

    @InjectMocks
    private BatchGenerationService service;

    private UUID calendarId;
    private ContentCalendar calendar;

    @BeforeEach
    void setUp() {
        calendarId = UUID.randomUUID();
        calendar = ContentCalendar.builder()
                .id(calendarId)
                .name("June Test")
                .periodStart(LocalDate.of(2026, 6, 1))
                .periodEnd(LocalDate.of(2026, 6, 7))
                .status(CalendarStatus.DRAFT)
                .build();
    }

    @Test
    void startBatch_createsPendingJobAndSetsGenerating() {
        CalendarItem item = CalendarItem.builder()
                .id(UUID.randomUUID()).calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 2))
                .channel("tiktok").status(CalendarItemStatus.PLANNED).build();

        when(calendarRepo.findById(calendarId)).thenReturn(Optional.of(calendar));
        when(calendarRepo.save(any())).thenReturn(calendar);
        when(itemRepo.findByCalendarIdAndStatus(calendarId, CalendarItemStatus.PLANNED))
                .thenReturn(List.of(item));

        BatchGenerationJob job = BatchGenerationJob.builder()
                .id(UUID.randomUUID()).calendarId(calendarId)
                .status(BatchJobStatus.RUNNING).totalItems(1).build();
        when(batchJobRepo.save(any())).thenReturn(job);

        ContentCalendarDto.BatchJobResponse resp = service.startBatch(calendarId);

        assertThat(resp.getStatus()).isEqualTo("RUNNING");
        assertThat(resp.getTotalItems()).isEqualTo(1);
        verify(calendarRepo).save(argThat(c -> c.getStatus() == CalendarStatus.GENERATING));
    }

    @Test
    void startBatch_throwsWhenNoPlannedItems() {
        when(calendarRepo.findById(calendarId)).thenReturn(Optional.of(calendar));
        when(itemRepo.findByCalendarIdAndStatus(calendarId, CalendarItemStatus.PLANNED))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.startBatch(calendarId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No PLANNED items");
    }

    @Test
    void startBatch_throwsWhenCalendarNotFound() {
        when(calendarRepo.findById(calendarId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startBatch(calendarId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getLatestJob_returnsJobForCalendar() {
        BatchGenerationJob job = BatchGenerationJob.builder()
                .id(UUID.randomUUID()).calendarId(calendarId)
                .status(BatchJobStatus.COMPLETED).totalItems(7).completedItems(7).build();
        when(calendarRepo.existsById(calendarId)).thenReturn(true);
        when(batchJobRepo.findFirstByCalendarIdOrderByCreatedAtDesc(calendarId))
                .thenReturn(Optional.of(job));

        ContentCalendarDto.BatchJobResponse resp = service.getLatestJob(calendarId);

        assertThat(resp.getStatus()).isEqualTo("COMPLETED");
        assertThat(resp.getCompletedItems()).isEqualTo(7);
    }

    @Test
    void getLatestJob_throwsWhenNoJobExists() {
        when(calendarRepo.existsById(calendarId)).thenReturn(true);
        when(batchJobRepo.findFirstByCalendarIdOrderByCreatedAtDesc(calendarId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatestJob(calendarId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void dispatchBatch_setsItemGeneratingAndCallsWorkflow() {
        UUID itemId = UUID.randomUUID();
        CalendarItem item = CalendarItem.builder()
                .id(itemId).calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 2))
                .channel("tiktok").status(CalendarItemStatus.PLANNED).build();

        when(itemRepo.findByCalendarIdAndStatus(calendarId, CalendarItemStatus.PLANNED))
                .thenReturn(List.of(item));
        when(itemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentWorkflow workflow = AgentWorkflow.builder()
                .id(UUID.randomUUID()).calendarItemId(itemId)
                .status(AgentWorkflowStatus.RUNNING).build();
        when(workflowService.createAndDispatchForCalendarItem(itemId)).thenReturn(workflow);

        service.dispatchBatch(calendarId, UUID.randomUUID());

        verify(workflowService).createAndDispatchForCalendarItem(itemId);
        verify(itemRepo, atLeastOnce()).save(argThat(i -> i.getStatus() == CalendarItemStatus.GENERATING));
    }

    @Test
    void dispatchBatch_setsItemFailedWhenDispatchThrows() {
        UUID itemId = UUID.randomUUID();
        CalendarItem item = CalendarItem.builder()
                .id(itemId).calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 2))
                .channel("tiktok").status(CalendarItemStatus.PLANNED).build();

        when(itemRepo.findByCalendarIdAndStatus(calendarId, CalendarItemStatus.PLANNED))
                .thenReturn(List.of(item));
        when(itemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(workflowService.createAndDispatchForCalendarItem(itemId))
                .thenThrow(new RuntimeException("worker down"));

        service.dispatchBatch(calendarId, UUID.randomUUID());

        verify(itemRepo, atLeastOnce()).save(argThat(i -> i.getStatus() == CalendarItemStatus.FAILED));
    }
}
