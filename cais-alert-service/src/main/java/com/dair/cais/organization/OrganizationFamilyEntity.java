package com.dair.cais.organization;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "cm_organization_family", schema = "info_alert")
public class OrganizationFamilyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "org_family_id")
    private Integer orgFamilyId;

    @Column(name = "org_key", nullable = false)
    private String orgKey;

    @Column(name = "parent_org_key")
    private String parentOrgKey;

    @Column(name = "org_family")
    private String orgFamily;
}
