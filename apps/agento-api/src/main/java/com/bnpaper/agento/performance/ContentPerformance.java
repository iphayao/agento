package com.bnpaper.agento.performance;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "content_performance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID generatedContentId;

    @Column(nullable = false, length = 50)
    private String channel;

    private LocalDateTime publishedAt;

    @Builder.Default
    private long impressions = 0;

    @Builder.Default
    private long views = 0;

    @Builder.Default
    private long clicks = 0;

    @Builder.Default
    private long likes = 0;

    @Builder.Default
    private long comments = 0;

    @Builder.Default
    private long shares = 0;

    @Builder.Default
    private long orders = 0;

    @Builder.Default
    @Column(precision = 14, scale = 2)
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(precision = 8, scale = 6)
    private BigDecimal conversionRate;

    @Column(precision = 8, scale = 6)
    private BigDecimal engagementRate;

    @Builder.Default
    @Column(precision = 14, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(precision = 10, scale = 4)
    private BigDecimal roas;

    @Column(columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
