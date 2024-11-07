package com.dair.cais.access.RolePolicyMapping;

import com.dair.cais.access.Role.RoleEntity;
import com.dair.cais.access.policy.PolicyEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cm_roles_policy_mapping", schema = "info_alert")
public class RolesPolicyMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rpm_seq")
    @SequenceGenerator(name = "rpm_seq", sequenceName = "info_alert.cm_roles_policy_mapping_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "rpm_id")
    private Integer rpmId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;
}


//package com.dair.cais.access.RolePolicyMapping;
//
//import com.dair.cais.access.policy.PolicyEntity;
//import com.dair.cais.access.Role.RoleEntity;
//import jakarta.persistence.*;
//import lombok.Data;
//
//@Data
//@Entity
//@Table(name = "cm_roles_policy_mapping", schema = "info_alert")
//public class RolesPolicyMappingEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rpm_seq")
//    @SequenceGenerator(name = "rpm_seq", sequenceName = "info_alert.cm_roles_policy_mapping_seq", allocationSize = 1, schema = "info_alert")
//    @Column(name = "rpm_id")
//    private Integer rpmId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "policy_id", nullable = false)
//    private PolicyEntity policy;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "role_id", nullable = false)
//    private RoleEntity role;
//}