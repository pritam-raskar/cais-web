package com.dair.cais.reports.repository;

import com.dair.cais.reports.ReportParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportParameterRepository extends JpaRepository<ReportParameterEntity, Long> {
    List<ReportParameterEntity> findByReport_ReportId(Long reportId);
    void deleteByReport_ReportId(Long reportId);
    // Find parameters by report ID
    @Query("SELECT p FROM ReportParameterEntity p WHERE p.report.reportId = :reportId")
    List<ReportParameterEntity> findByReportId(@Param("reportId") Integer reportId);

    // Delete parameters by report ID
    @Modifying
    @Query("DELETE FROM ReportParameterEntity p WHERE p.report.reportId = :reportId")
    void deleteByReportId(@Param("reportId") Integer reportId);
}