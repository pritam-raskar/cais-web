package com.dair.cais.steps;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_steps", schema = "info_alert")
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "step_id_seq")
    @SequenceGenerator(name = "step_id_seq", sequenceName = "info_alert.cm_steps_step_id_seq", allocationSize = 1)
    @Column(name = "step_id")
    private Long stepId;

    @Column(name = "step_name")
    private String stepName;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "step_status_id")
    private Integer stepStatusId;
}