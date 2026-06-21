package com.bnpaper.agento.export;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "export_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportType exportType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExportStatus status = ExportStatus.PENDING;

    private UUID calendarId;
    private UUID campaignId;
    private String channel;
    private LocalDate startDate;
    private LocalDate endDate;

    @Column(columnDefinition = "text")
    private String includeStatuses;

    @Column(columnDefinition = "text")
    private String fileUrl;

    private String fileName;
    private Integer rowCount;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
