package com.dair.cais.workflow.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_wf_step_checklist", schema = "info_alert")
public class ChecklistEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checklist_seq")
    @SequenceGenerator(
            name = "checklist_seq",
            sequenceName = "info_alert.cm_wf_step_checklist_id_seq",
            allocationSize = 1,
            schema = "info_alert"
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "list_name", length = 4000, nullable = false)
    private String listName;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "created_by", length = 255, nullable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

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