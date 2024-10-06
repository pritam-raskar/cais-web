package com.dair.cais.steps;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_step_status", schema = "info_alert")
public class StepStatus {
    @Id
    @Column(name = "step_status_id")
    private Integer stepStatusId;

    @Column(name = "step_name")
    private String stepName;
}