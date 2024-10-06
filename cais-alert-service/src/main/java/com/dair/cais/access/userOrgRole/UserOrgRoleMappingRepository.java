package com.dair.cais.access.userOrgRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserOrgRoleMappingRepository extends JpaRepository<UserOrgRoleMappingEntity, Long> {
    List<UserOrgRoleMappingEntity> findByUserUserId(String userId);
}