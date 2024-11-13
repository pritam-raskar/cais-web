package com.dair.cais.reports.mapper;

import com.dair.cais.reports.ReportsEntity;
import com.dair.cais.reports.dto.ReportSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class ReportSummaryMapper {

    public ReportSummaryDto toSummaryDto(ReportsEntity entity) {
        return ReportSummaryDto.builder()
                .reportId(entity.getReportId())
                .reportIdentifier(entity.getReportIdentifier())
                .reportName(entity.getReportName())
                .reportDescription(entity.getReportDescription())
                .reportType(entity.getReportType())
                .status(entity.getStatus())
                .isPublished(entity.getIsPublished())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}