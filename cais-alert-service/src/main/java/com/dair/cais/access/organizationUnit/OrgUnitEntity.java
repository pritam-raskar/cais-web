package com.dair.cais.access.organizationUnit;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_organization_unit", schema = "info_alert")
public class OrgUnitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_seq")
    @SequenceGenerator(name = "org_seq", sequenceName = "info_alert.business_units_bu_id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "org_id")
    private Integer orgId;

    @Column(name = "org_name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "org_description")
    private String description;
}