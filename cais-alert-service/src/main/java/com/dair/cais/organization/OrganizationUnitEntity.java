package com.dair.cais.organization;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_organization_unit", schema = "info_alert")
public class OrganizationUnitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_unit_seq")
    @SequenceGenerator(name = "org_unit_seq", sequenceName = "info_alert.business_units_bu_id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "org_id")
    private Integer orgId;

    @Column(name = "type")
    private String type;

    @Column(name = "org_key", unique = true, nullable = false)
    private String orgKey;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "org_description")
    private String orgDescription;

    @CreationTimestamp
    @Column(name = "create_date", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "update_date")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
