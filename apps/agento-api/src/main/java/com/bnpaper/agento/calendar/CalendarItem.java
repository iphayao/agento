package com.bnpaper.agento.calendar;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "calendar_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID calendarId;

    @Column(nullable = false)
    private LocalDate plannedDate;

    @Column(nullable = false)
    private String channel;

    private String contentType;

    @Column(columnDefinition = "text")
    private String contentAngle;

    @Column(columnDefinition = "text")
    private String targetAudience;

    @Column(columnDefinition = "text")
    private String hookDirection;

    @Column(columnDefinition = "text")
    private String ctaDirection;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CalendarItemStatus status = CalendarItemStatus.PLANNED;

    private UUID generatedContentId;

    private UUID workflowId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
