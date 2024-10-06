package com.dair.cais.access.Role;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_roles", schema = "info_alert")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @SequenceGenerator(name = "role_seq", sequenceName = "info_alert.cm_roles_new_column_name_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "description")
    private String description;

    @Column(name = "role_identifier", nullable = false)
    private String roleIdentifier;
}