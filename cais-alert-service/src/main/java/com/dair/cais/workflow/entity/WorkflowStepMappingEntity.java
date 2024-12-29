package com.dair.cais.workflow.entity;

import com.dair.cais.steps.Step;
import com.dair.cais.workflow.model.Workflow;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity representing the mapping between workflows and steps.
 * This maps to the info_alert.cm_wf_step_mapping table.
 */
@Data
@Entity
@Table(name = "cm_wf_step_mapping", schema = "info_alert")
public class WorkflowStepMappingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_step_mapping_seq")
    @SequenceGenerator(
            name = "workflow_step_mapping_seq",
            sequenceName = "info_alert.cm_wf_step_mapping_workflow_step_id_seq",
            allocationSize = 1,
            schema = "info_alert"
    )
    @Column(name = "workflow_step_id")
    private Long workflowStepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowEntity workflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    @Column(name = "position_number")
    private Integer positionNumber;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}