package com.dair.cais.report;

import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public Report toModel(ReportEntity entity) {
        Report report = new Report();
        report.setId(entity.getReportId());
        report.setName(entity.getName());

        report.setCreatedDate(entity.getCreatedDate());
        report.setUpdatedDate(entity.getCreatedDate());

        return report;
    }

    public Report toModel(String tableName) {
        Report report = new Report();
        report.setName(tableName);

        return report;
    }

    public ReportEntity toEntity(String extractReportId, Report report) {
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setReportId(extractReportId);
        reportEntity.setName(report.getName());

        reportEntity.setCreatedDate(report.getCreatedDate());
        reportEntity.setUpdatedDate(report.getUpdatedDate());

        return reportEntity;
    }

}