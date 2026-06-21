package com.bnpaper.agento.calendar;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_generation_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchGenerationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID calendarId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BatchJobStatus status = BatchJobStatus.PENDING;

    @Builder.Default
    private int totalItems = 0;

    @Builder.Default
    private int completedItems = 0;

    @Builder.Default
    private int failedItems = 0;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
