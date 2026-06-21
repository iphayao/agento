package com.bnpaper.agento.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BatchGenerationJobRepository extends JpaRepository<BatchGenerationJob, UUID> {
    Optional<BatchGenerationJob> findFirstByCalendarIdOrderByCreatedAtDesc(UUID calendarId);
}
