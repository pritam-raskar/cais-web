package com.dair.cais.reports.repository;

import com.dair.cais.reports.ReportsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportsRepository extends JpaRepository<ReportsEntity, Integer>  {
    Optional<ReportsEntity> findByReportIdentifier(String reportIdentifier);

    List<ReportsEntity> findByConnectionId(Long connectionId);

    @Query("SELECT r FROM ReportsEntity r WHERE " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:isPublished IS NULL OR r.isPublished = :isPublished) AND " +
            "(:searchTerm IS NULL OR " +
            "LOWER(r.reportName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.reportDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ReportsEntity> findByFilters(
            @Param("status") String status,
            @Param("isPublished") Boolean isPublished,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    boolean existsByReportIdentifier(String reportIdentifier);

    // Find report with its parameters
    @Query("SELECT r FROM ReportsEntity r LEFT JOIN FETCH r.parameters WHERE r.reportId = :reportId")
    Optional<ReportsEntity> findByIdWithParameters(@Param("reportId") Integer reportId);

    // Find report with columns and parameters
    @Query("SELECT DISTINCT r FROM ReportsEntity r " +
            "LEFT JOIN FETCH r.columns c " +
            "LEFT JOIN FETCH r.parameters p " +
            "WHERE r.reportId = :reportId")
    Optional<ReportsEntity> findByIdWithColumnsAndParameters(@Param("reportId") Integer reportId);

    // Search reports with their parameters
    @Query("SELECT DISTINCT r FROM ReportsEntity r " +
            "LEFT JOIN FETCH r.parameters " +
            "WHERE (:status IS NULL OR r.status = :status) " +
            "AND (:isPublished IS NULL OR r.isPublished = :isPublished) " +
            "AND (:searchTerm IS NULL OR " +
            "LOWER(r.reportName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.reportDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ReportsEntity> findByFiltersWithParameters(
            @Param("status") String status,
            @Param("isPublished") Boolean isPublished,
            @Param("searchTerm") String searchTerm);
}