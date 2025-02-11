package com.dair.cais.workflow.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_workflow_transition_reason_mapping", schema = "info_alert")
public class WorkflowTransitionReasonMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_transition_reason_mapping_seq")
    @SequenceGenerator(
            name = "workflow_transition_reason_mapping_seq",
            sequenceName = "info_alert.cm_workflow_transition_reason_mapping_id_seq",
            allocationSize = 1,
            schema = "info_alert"
    )
    @Column(name = "transition_reason_mapping_id")
    private Long transitionReasonMappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transition_id", nullable = false)
    private WorkflowTransitionEntity transition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id", nullable = false)
    private TransitionReasonEntity reason;
}
