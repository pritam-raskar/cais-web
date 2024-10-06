package com.dair.cais.organization;

import com.dair.cais.organization.OrganizationUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnitEntity, Integer> {
    Optional<OrganizationUnitEntity> findByOrgKey(String orgKey);
}
