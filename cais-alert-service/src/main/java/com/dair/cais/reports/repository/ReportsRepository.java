package com.dair.cais.reports.repository;

import com.dair.cais.reports.ReportsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReportsRepository extends JpaRepository<ReportsEntity, Integer>  {
    Optional<ReportsEntity> findByReportIdentifier(String reportIdentifier);
    Optional<ReportsEntity> findByReportId(Integer reportId);

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

    @Query("""
        SELECT NEW map(
            COUNT(r) as totalReports,
            SUM(CASE WHEN r.isPublished = true THEN 1 ELSE 0 END) as publishedReports,
            SUM(CASE WHEN r.status = 'DRAFT' THEN 1 ELSE 0 END) as draftReports,
            SUM(CASE WHEN r.status = 'ARCHIVED' THEN 1 ELSE 0 END) as archivedReports,
            MAX(r.updatedAt) as lastUpdated
        )
        FROM ReportsEntity r
    """)
    Map<String, Object> getReportStatistics();

    @Query("SELECT r FROM ReportsEntity r ORDER BY r.updatedAt DESC")
    List<ReportsEntity> findAllReportsBasicInfo();

    /**
     * Finds all reports that are marked as tabs
     *
     * @return List of reports that are tabs
     */
    @Query("SELECT r FROM ReportsEntity r WHERE r.isTab = true ORDER BY r.reportName")
    List<ReportsEntity> findByIsTabTrue();
}