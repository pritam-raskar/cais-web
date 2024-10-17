package com.dair.cais.audit;

import jakarta.persistence.*;
import lombok.Data;
import com.dair.cais.access.Actions.ActionEntity;
import java.time.ZonedDateTime;

@Entity
@Data
@Table(name = "cm_audit_trail" , schema = "info_alert")
public class AuditTrailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cm_audit_sequence")
    @SequenceGenerator(name = "cm_audit_sequence", sequenceName = "cm_audit_sequence", allocationSize = 1, schema = "info_alert")
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne
    @JoinColumn(name = "action_id")
    private ActionEntity action;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_role", nullable = false)
    private String userRole;

    @Column(name = "action_timestamp")
    private ZonedDateTime actionTimestamp;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "affected_item_type")
    private String affectedItemType;

    @Column(name = "affected_item_id")
    private String affectedItemId;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;
}