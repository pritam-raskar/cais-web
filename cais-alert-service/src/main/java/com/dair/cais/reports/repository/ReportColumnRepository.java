package com.dair.cais.reports.repository;


import com.dair.cais.reports.ReportColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportColumnRepository extends JpaRepository<ReportColumnEntity, Integer> {
    List<ReportColumnEntity> findByReportIdOrderBySortPriorityAsc(Integer reportId);

    void deleteByReportId(Long reportId);
}
