package com.bnpaper.agento.product;

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
@Table(name = "product_facts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String productName;

    @Column(unique = true)
    private String sku;

    @Builder.Default
    private int sheetCount = 180;

    @Builder.Default
    private int ply = 2;

    @Builder.Default
    private int packSize = 5;

    @Builder.Default
    private int cartonSize = 50;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    @Builder.Default
    private List<String> keyBenefits = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "text")
    @Builder.Default
    private List<String> proofPoints = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
