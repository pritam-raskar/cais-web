package com.dair.cais.steps;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_steps", schema = "info_alert")
public class Step {
    @Id
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