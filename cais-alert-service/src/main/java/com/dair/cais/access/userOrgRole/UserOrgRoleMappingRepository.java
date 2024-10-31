package com.dair.cais.access.userOrgRole;

import com.dair.cais.access.Role.RoleEntity;
import com.dair.cais.access.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserOrgRoleMappingRepository extends JpaRepository<UserOrgRoleMappingEntity, Long> {
    List<UserOrgRoleMappingEntity> findByUserUserId(String userId);
    boolean existsByRole(RoleEntity role);

    @Query("SELECT count(DISTINCT m.user) FROM UserOrgRoleMappingEntity m WHERE m.role = :role")
    long countByRole(RoleEntity role);

    @Query("SELECT DISTINCT m.user FROM UserOrgRoleMappingEntity m WHERE m.role = :role")
    List<UserEntity> findUsersByRole(RoleEntity role);

    List<UserOrgRoleMappingEntity> findByRoleRoleId(Integer roleId);
}