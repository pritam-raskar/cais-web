package com.dair.cais.workflow.entity;

import com.dair.cais.steps.Step;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_workflow_step", schema = "info_alert")
public class WorkflowStepEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_step_seq")
    @SequenceGenerator(
            name = "workflow_step_seq",
            sequenceName = "info_alert.cm_workflow_step_id_seq",
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

    @Column(name = "position_x", nullable = false)
    private Integer positionX;

    @Column(name = "position_y", nullable = false)
    private Integer positionY;

    @Column(name = "label")
    private String label;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "created_by", length = 255, nullable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @OneToOne(mappedBy = "workflowStep", cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkflowStepDeadlineEntity deadline;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = createdDate;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}

