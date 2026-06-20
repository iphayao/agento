package com.bnpaper.agento.performance;

import com.bnpaper.agento.common.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "performance_summaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(length = 50)
    private String channel;

    @Column(nullable = false, columnDefinition = "text")
    private String summaryText;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    private List<String> recommendedAngles;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    private List<String> recommendedHooks;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    private List<String> recommendedCTAs;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    private List<String> avoidPatterns;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
