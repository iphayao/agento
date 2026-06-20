package com.bnpaper.agento.workflow;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_step_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentStepResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID workflowId;

    @Column(nullable = false)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AgentStepStatus status = AgentStepStatus.PENDING;

    @Column(columnDefinition = "text")
    private String inputPayload;

    @Column(columnDefinition = "text")
    private String outputPayload;

    @Column(columnDefinition = "text")
    private String errorMessage;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}
