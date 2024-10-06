package com.dair.cais.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, String> {
    Page<ReportEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

}