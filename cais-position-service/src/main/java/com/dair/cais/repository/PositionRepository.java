package com.dair.cais.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dair.cais.entity.PositionEntity;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Repository
public interface PositionRepository extends JpaRepository<PositionEntity, String> {
    Page<PositionEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}