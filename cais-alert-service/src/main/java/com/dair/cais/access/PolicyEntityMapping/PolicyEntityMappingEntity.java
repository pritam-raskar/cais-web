package com.dair.cais.access.PolicyEntityMapping;

import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.entity.SystemEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_policy_entity_mapping", schema = "info_alert",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"policy_id", "entity_type", "entity_id", "action_id"})
        })
public class PolicyEntityMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "policy_entity_mapping_seq")
    @SequenceGenerator(
            name = "policy_entity_mapping_seq",
            sequenceName = "info_alert.cm_policy_entity_mapping_seq",
            allocationSize = 1,
            schema = "info_alert"
    )
    @Column(name = "mapping_id")
    private Integer mappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ActionEntity action;

    @Column(name = "condition")
    private String condition;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}