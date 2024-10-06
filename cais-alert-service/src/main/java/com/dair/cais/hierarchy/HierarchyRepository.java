package com.dair.cais.hierarchy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HierarchyRepository extends JpaRepository<HierarchyEntity, Integer> {
}
