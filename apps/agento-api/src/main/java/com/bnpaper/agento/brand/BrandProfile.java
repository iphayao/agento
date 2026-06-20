package com.bnpaper.agento.brand;

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
@Table(name = "brand_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String brandName;

    @Column(columnDefinition = "text")
    private String slogan;

    @Column(columnDefinition = "text")
    private String toneOfVoice;

    @Column(columnDefinition = "text")
    private String targetAudience;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    @Builder.Default
    private List<String> keyMessages = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    @Builder.Default
    private List<String> prohibitedClaims = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
