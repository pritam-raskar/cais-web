package com.dair.cais.access.RolePolicyMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolesPolicyMappingRepository extends JpaRepository<RolesPolicyMappingEntity, Integer> {
    List<RolesPolicyMappingEntity> findByPolicyPolicyId(Integer policyId);
    List<RolesPolicyMappingEntity> findByRoleRoleId(Integer roleId);
}