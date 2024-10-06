package com.dair.cais.access.Actions;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_actions", schema = "info_alert")
public class ActionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cm_action_sequence")
    @SequenceGenerator(name = "cm_action_sequence", sequenceName = "info_alert.cm_action_sequence", allocationSize = 1, schema = "info_alert")
    @Column(name = "action_id")
    private Integer actionId;

    @Column(name = "action_name")
    private String actionName;

    @Column(name = "action_category")
    private String actionCategory;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "action_description")
    private String actionDescription;

    @Column(name = "is_role_action")
    private Boolean isRoleAction;
}