package com.dair.cais.workflow.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_workflow_step_deadline", schema = "info_alert")
public class WorkflowStepDeadlineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_step_deadline_seq")
    @SequenceGenerator(
            name = "workflow_step_deadline_seq",
            sequenceName = "info_alert.cm_workflow_step_deadline_id_seq",
            allocationSize = 1,
            schema = "info_alert"
    )
    @Column(name = "deadline_id")
    private Long deadlineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStepEntity workflowStep;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "count_value")
    private Integer countValue;

    @Column(name = "measure_unit", length = 50)
    private String measureUnit;

    @Column(name = "email_reminder_active")
    private Boolean emailReminderActive;

    @Column(name = "email_reminder_count")
    private Integer emailReminderCount;

    @Column(name = "email_reminder_measure", length = 50)
    private String emailReminderMeasure;

    @Column(name = "step_change_active")
    private Boolean stepChangeActive;

    @Column(name = "target_step_id")
    private Long targetStepId;
}