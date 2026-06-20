package com.bnpaper.agento.performance;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "content_insights")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID generatedContentId;

    private UUID campaignId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InsightType insightType;

    @Column(nullable = false, columnDefinition = "text")
    private String insightText;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidenceScore = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
