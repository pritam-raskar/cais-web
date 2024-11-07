package com.dair.cais.access.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemEntityRepository extends JpaRepository<SystemEntityJpa, Integer> {
    Optional<SystemEntityJpa> findByEntityType(String entityType);
    List<SystemEntityJpa> findByIsActiveTrue();
    boolean existsByEntityType(String entityType);
}