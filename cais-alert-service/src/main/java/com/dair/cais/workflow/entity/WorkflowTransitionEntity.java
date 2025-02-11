package com.dair.cais.workflow.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cm_workflow_transition", schema = "info_alert")
@Data
public class WorkflowTransitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_transition_seq")
    @SequenceGenerator(
            name = "workflow_transition_seq",
            sequenceName = "info_alert.cm_workflow_transition_seq",
            allocationSize = 1
    )
    @Column(name = "transition_id")
    private Long transitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private WorkflowEntity workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_step_id")
    private WorkflowStepEntity sourceStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_step_id")
    private WorkflowStepEntity targetStep;

    @Column(name = "allow_automatic_transition")
    private Boolean allowAutomaticTransition = false;

    @Column(name = "required_note")
    private Boolean requiredNote = false;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdDate == null) {
            createdDate = now;
        }
        if (updatedDate == null) {
            updatedDate = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "transition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkflowTransitionReasonMappingEntity> reasonMappings = new ArrayList<>();
}