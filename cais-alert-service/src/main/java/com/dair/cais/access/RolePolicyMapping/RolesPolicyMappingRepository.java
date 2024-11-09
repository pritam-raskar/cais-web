package com.dair.cais.access.RolePolicyMapping;

import com.dair.cais.access.policy.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolesPolicyMappingRepository extends JpaRepository<RolesPolicyMappingEntity, Integer> {
    @Query("SELECT rpm FROM RolesPolicyMappingEntity rpm " +
            "LEFT JOIN FETCH rpm.policy " +
            "LEFT JOIN FETCH rpm.role")
    List<RolesPolicyMappingEntity> findAllWithPolicyAndRole();

    @Query("SELECT rpm FROM RolesPolicyMappingEntity rpm " +
            "LEFT JOIN FETCH rpm.policy " +
            "LEFT JOIN FETCH rpm.role " +
            "WHERE rpm.rpmId = :rpmId")
    Optional<RolesPolicyMappingEntity> findByIdWithPolicyAndRole(Integer rpmId);

    @Query("SELECT rpm FROM RolesPolicyMappingEntity rpm " +
            "LEFT JOIN FETCH rpm.policy " +
            "LEFT JOIN FETCH rpm.role " +
            "WHERE rpm.policy.policyId = :policyId")
    List<RolesPolicyMappingEntity> findByPolicyPolicyIdWithPolicyAndRole(Integer policyId);

    @Query("SELECT rpm FROM RolesPolicyMappingEntity rpm " +
            "LEFT JOIN FETCH rpm.policy " +
            "LEFT JOIN FETCH rpm.role " +
            "WHERE rpm.role.roleId = :roleId")
    List<RolesPolicyMappingEntity> findByRoleRoleIdWithPolicyAndRole(Integer roleId);

    @Query("SELECT rpm FROM RolesPolicyMappingEntity rpm " +
            "LEFT JOIN FETCH rpm.policy " +
            "LEFT JOIN FETCH rpm.role " +
            "WHERE rpm.policy.policyId = :policyId AND rpm.role.roleId = :roleId")
    Optional<RolesPolicyMappingEntity> findByPolicyIdAndRoleIdWithPolicyAndRole(Integer policyId, Integer roleId);

        List<RolesPolicyMappingEntity> findByPolicyPolicyId(Integer policyId);
    List<RolesPolicyMappingEntity> findByRoleRoleId(Integer roleId);

    Optional<RolesPolicyMappingEntity> findByPolicyPolicyIdAndRoleRoleId(Integer policyId, Integer roleId);


    @Query("SELECT rpm FROM RolesPolicyMappingEntity rpm " +
            "WHERE rpm.role.roleId = :roleId " +
            "AND rpm.policy.policyId IN :policyIds")
    List<RolesPolicyMappingEntity> findByRoleRoleIdAndPolicyPolicyIdIn(
            @Param("roleId") Integer roleId,
            @Param("policyIds") List<Integer> policyIds);

    @Query("SELECT rpm FROM RolesPolicyMappingEntity rpm " +
            "WHERE rpm.role.roleId = :roleId " +
            "AND rpm.policy.policyId = :policyId")
    Optional<RolesPolicyMappingEntity> findByRoleRoleIdAndPolicyPolicyId(
            @Param("roleId") Integer roleId,
            @Param("policyId") Integer policyId);

    @Query("SELECT DISTINCT r.roleName FROM RoleEntity r " +
            "JOIN RolesPolicyMappingEntity rpm ON rpm.role = r " +
            "WHERE rpm.policy = :policy")
    List<String> findRoleNamesByPolicy(@Param("policy") PolicyEntity policy);

    void deleteByPolicy(PolicyEntity policy);
}




//package com.dair.cais.access.RolePolicyMapping;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface RolesPolicyMappingRepository extends JpaRepository<RolesPolicyMappingEntity, Integer> {
//    List<RolesPolicyMappingEntity> findByPolicyPolicyId(Integer policyId);
//    List<RolesPolicyMappingEntity> findByRoleRoleId(Integer roleId);
//
//    Optional<RolesPolicyMappingEntity> findByPolicyPolicyIdAndRoleRoleId(Integer policyId, Integer roleId);
//}