package com.dair.cais.access.policy;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_policy", schema = "info_alert")
public class PolicyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "policy_seq")
    @SequenceGenerator(name = "policy_seq", sequenceName = "info_alert.cm_policy_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "policy_id")
    private Integer policyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}