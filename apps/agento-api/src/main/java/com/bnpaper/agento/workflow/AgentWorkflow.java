package com.bnpaper.agento.workflow;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_workflows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID campaignId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AgentWorkflowStatus status = AgentWorkflowStatus.PENDING;

    private String currentStep;

    @Column(columnDefinition = "text")
    private String inputPayload;

    @Column(columnDefinition = "text")
    private String outputPayload;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
