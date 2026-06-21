package com.bnpaper.agento.calendar;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/content-calendars")
@RequiredArgsConstructor
public class ContentCalendarController {

    private final ContentCalendarService calendarService;
    private final BatchGenerationService batchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentCalendarDto.CalendarResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(calendarService.listCalendars()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContentCalendarDto.CalendarResponse>> create(
            @Valid @RequestBody ContentCalendarDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(calendarService.createCalendar(req), "Calendar created"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentCalendarDto.CalendarResponse>> findById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(calendarService.findCalendar(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentCalendarDto.CalendarResponse>> update(
            @PathVariable UUID id,
            @RequestBody ContentCalendarDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(calendarService.updateCalendar(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        calendarService.deleteCalendar(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Calendar deleted"));
    }

    // ── Items ──────────────────────────────────────────────────────────────────

    @GetMapping("/{calendarId}/items")
    public ResponseEntity<ApiResponse<List<ContentCalendarDto.ItemResponse>>> listItems(
            @PathVariable UUID calendarId) {
        return ResponseEntity.ok(ApiResponse.success(calendarService.listItems(calendarId)));
    }

    @PostMapping("/{calendarId}/items")
    public ResponseEntity<ApiResponse<ContentCalendarDto.ItemResponse>> addItem(
            @PathVariable UUID calendarId,
            @Valid @RequestBody ContentCalendarDto.ItemCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(calendarService.addItem(calendarId, req), "Item added"));
    }

    @PutMapping("/{calendarId}/items/{itemId}")
    public ResponseEntity<ApiResponse<ContentCalendarDto.ItemResponse>> updateItem(
            @PathVariable UUID calendarId,
            @PathVariable UUID itemId,
            @RequestBody ContentCalendarDto.ItemUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.updateItem(calendarId, itemId, req)));
    }

    @DeleteMapping("/{calendarId}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable UUID calendarId,
            @PathVariable UUID itemId) {
        calendarService.deleteItem(calendarId, itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item deleted"));
    }

    // ── AI Planning ───────────────────────────────────────────────────────────

    @PostMapping("/{calendarId}/plan")
    public ResponseEntity<ApiResponse<List<ContentCalendarDto.ItemResponse>>> planCalendar(
            @PathVariable UUID calendarId) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.planCalendar(calendarId), "Calendar plan generated"));
    }

    // ── Batch Generation ──────────────────────────────────────────────────────

    @PostMapping("/{calendarId}/generate")
    public ResponseEntity<ApiResponse<ContentCalendarDto.BatchJobResponse>> startBatch(
            @PathVariable UUID calendarId) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(batchService.startBatch(calendarId),
                        "Batch generation started"));
    }

    @GetMapping("/{calendarId}/batch-job")
    public ResponseEntity<ApiResponse<ContentCalendarDto.BatchJobResponse>> getLatestJob(
            @PathVariable UUID calendarId) {
        return ResponseEntity.ok(ApiResponse.success(batchService.getLatestJob(calendarId)));
    }
}
