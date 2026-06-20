package com.bnpaper.agento.content;

import com.bnpaper.agento.common.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "generated_content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID campaignId;

    private String contentType;

    private String channel;

    @Column(columnDefinition = "text")
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @Column(columnDefinition = "text")
    private String hook;

    @Column(columnDefinition = "text")
    private String callToAction;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    @Builder.Default
    private List<String> hashtags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ContentStatus status = ContentStatus.DRAFT;

    private String aiModel;

    private String promptVersion;

    @Column(columnDefinition = "text")
    private String complianceNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
