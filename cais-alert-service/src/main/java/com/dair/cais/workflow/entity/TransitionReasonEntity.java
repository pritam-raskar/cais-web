package com.dair.cais.workflow.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_wf_transition_reason", schema = "info_alert")
public class TransitionReasonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reason_seq")
    @SequenceGenerator(
            name = "reason_seq",
            sequenceName = "info_alert.cm_wf_transition_reason_id_seq",
            allocationSize = 1,
            schema = "info_alert"
    )
    @Column(name = "id")
    private Long id;

    @Column(name = "reason_details", length = 4000, nullable = false)
    private String reasonDetails;

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