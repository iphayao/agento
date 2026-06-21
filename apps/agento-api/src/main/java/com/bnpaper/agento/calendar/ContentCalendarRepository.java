package com.bnpaper.agento.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContentCalendarRepository extends JpaRepository<ContentCalendar, UUID> {
    List<ContentCalendar> findAllByOrderByCreatedAtDesc();
}
