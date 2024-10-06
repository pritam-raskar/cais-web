package com.dair.cais.access.userOrgRole;

import com.dair.cais.access.Role.RoleEntity;
import com.dair.cais.access.organizationUnit.OrgUnitEntity;
import com.dair.cais.access.user.UserEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "CM_user_org_role_mapping", schema = "info_alert")
public class UserOrgRoleMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_org_role_seq")
    @SequenceGenerator(name = "user_org_role_seq", sequenceName = "info_alert.cm_user_org_role_mapping_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "orm_id")
    private Long mappingId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private OrgUnitEntity orgUnit;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleEntity role;
}
