package com.bnpaper.agento.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CalendarItemRepository extends JpaRepository<CalendarItem, UUID> {
    List<CalendarItem> findByCalendarIdOrderByPlannedDateAsc(UUID calendarId);
    List<CalendarItem> findByCalendarIdAndStatus(UUID calendarId, CalendarItemStatus status);
    int countByCalendarId(UUID calendarId);
    void deleteByCalendarId(UUID calendarId);
}
