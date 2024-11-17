// File: com/dair/cais/filter/repository/FilterEntityTypeRepository.java
package com.dair.cais.filter.repository;

import com.dair.cais.filter.domain.FilterEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilterEntityTypeRepository extends JpaRepository<FilterEntityType, Long> {
    List<FilterEntityType> findByIsActiveTrue();
    Optional<FilterEntityType> findByEntityNameIgnoreCase(String entityName);
    boolean existsByEntityNameIgnoreCase(String entityName);
}

