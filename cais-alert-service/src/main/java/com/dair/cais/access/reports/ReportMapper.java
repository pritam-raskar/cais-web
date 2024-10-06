package com.dair.cais.access.reports;

import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public Report toDto(ReportEntity entity) {
        if (entity == null) {
            return null;
        }

        Report dto = new Report();
        dto.setReportId(entity.getReportId());
        dto.setReportName(entity.getReportName());
        dto.setReportDescription(entity.getReportDescription());
        dto.setConnectorId(entity.getConnectorId());
        dto.setTableOrViewName(entity.getTableOrViewName());
        
        return dto;
    }

    public ReportEntity toEntity(Report dto) {
        if (dto == null) {
            return null;
        }

        ReportEntity entity = new ReportEntity();
        entity.setReportId(dto.getReportId());
        entity.setReportName(dto.getReportName());
        entity.setReportDescription(dto.getReportDescription());
        entity.setConnectorId(dto.getConnectorId());
        entity.setTableOrViewName(dto.getTableOrViewName());
        
        return entity;
    }
}
