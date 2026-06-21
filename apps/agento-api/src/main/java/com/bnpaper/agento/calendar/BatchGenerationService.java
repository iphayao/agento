package com.bnpaper.agento.calendar;

import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.workflow.AgentWorkflow;
import com.bnpaper.agento.workflow.AgentWorkflowService;
import com.bnpaper.agento.workflow.AgentWorkflowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchGenerationService {

    private final BatchGenerationJobRepository batchJobRepo;
    private final CalendarItemRepository itemRepo;
    private final ContentCalendarRepository calendarRepo;
    private final AgentWorkflowService workflowService;

    @Transactional
    public ContentCalendarDto.BatchJobResponse startBatch(UUID calendarId) {
        ContentCalendar cal = calendarRepo.findById(calendarId)
                .orElseThrow(() -> new ResourceNotFoundException("ContentCalendar", calendarId));

        List<CalendarItem> plannedItems = itemRepo.findByCalendarIdAndStatus(
                calendarId, CalendarItemStatus.PLANNED);

        if (plannedItems.isEmpty()) {
            throw new IllegalStateException(
                    "No PLANNED items to generate. Add or plan items first.");
        }

        cal.setStatus(CalendarStatus.GENERATING);
        calendarRepo.save(cal);

        BatchGenerationJob job = BatchGenerationJob.builder()
                .calendarId(calendarId)
                .status(BatchJobStatus.RUNNING)
                .totalItems(plannedItems.size())
                .completedItems(0)
                .failedItems(0)
                .build();
        job = batchJobRepo.save(job);

        dispatchBatch(calendarId, job.getId());

        return ContentCalendarDto.toBatchJobResponse(job);
    }

    public ContentCalendarDto.BatchJobResponse getLatestJob(UUID calendarId) {
        if (!calendarRepo.existsById(calendarId)) {
            throw new ResourceNotFoundException("ContentCalendar", calendarId);
        }
        BatchGenerationJob job = batchJobRepo
                .findFirstByCalendarIdOrderByCreatedAtDesc(calendarId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No batch job found for calendar " + calendarId));
        return ContentCalendarDto.toBatchJobResponse(job);
    }

    /** Dispatches each PLANNED item to the workflow service. Runs asynchronously. */
    @Async("batchExecutor")
    protected void dispatchBatch(UUID calendarId, UUID jobId) {
        List<CalendarItem> items = itemRepo.findByCalendarIdAndStatus(
                calendarId, CalendarItemStatus.PLANNED);

        log.info("Batch job {} dispatching {} items for calendar {}", jobId, items.size(), calendarId);

        for (CalendarItem item : items) {
            try {
                item.setStatus(CalendarItemStatus.GENERATING);
                itemRepo.save(item);

                AgentWorkflow workflow = workflowService.createAndDispatchForCalendarItem(item.getId());

                if (workflow.getStatus() == AgentWorkflowStatus.FAILED) {
                    log.warn("Batch: item {} dispatch failed immediately", item.getId());
                }
            } catch (Exception e) {
                log.error("Batch: failed to dispatch item {}: {}", item.getId(), e.getMessage(), e);
                item.setStatus(CalendarItemStatus.FAILED);
                itemRepo.save(item);
            }
        }

        log.info("Batch job {} dispatch loop complete for calendar {}", jobId, calendarId);
    }
}
