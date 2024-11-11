package com.dair.cais.reports;

import com.dair.cais.reports.dto.FormattingConfigDto;
import com.dair.cais.reports.dto.ReportColumnCreateDto;
import com.dair.cais.reports.dto.ReportColumnDto;
import com.dair.cais.reports.dto.ReportColumnUpdateDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapper class for converting between Report entities and DTOs.
 * Handles all necessary transformations and JSON serialization/deserialization.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReportsMapper {
    private final ObjectMapper objectMapper;

    // ... other methods remain the same ...

    public ReportColumnDto toColumnDto(ReportColumnEntity entity) {
        ReportColumnDto dto = new ReportColumnDto();

        // Map base fields
        dto.setCrcId(entity.getCrcId());
        dto.setReportId(entity.getReportId());
        dto.setSourceColumn(entity.getSourceColumn());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDataType(entity.getDataType());
        dto.setIsSortable(entity.getIsSortable());
        dto.setIsFilterable(entity.getIsFilterable());
        dto.setIsExportable(entity.getIsExportable());
        dto.setIsVisible(entity.getIsVisible());
        dto.setSortPriority(entity.getSortPriority());
        dto.setSortDirection(entity.getSortDirection());
        dto.setColumnWidth(entity.getColumnWidth());
        dto.setAlignment(entity.getAlignment());

        // Map formatting JSON
        if (entity.getFormattingJson() != null) {
            try {
                FormattingConfigDto formattingConfig = objectMapper.convertValue(
                        entity.getFormattingJson(),
                        FormattingConfigDto.class
                );
                dto.setFormattingJson(formattingConfig);
            } catch (Exception e) {
                log.error("Error converting formatting JSON for column {}: {}",
                        entity.getCrcId(), e.getMessage());
            }
        }

        return dto;
    }

    public ReportColumnEntity toColumnEntity(ReportColumnCreateDto dto, Integer reportId) {
        ReportColumnEntity entity = new ReportColumnEntity();

        // Set basic fields
        entity.setReportId(reportId);
        entity.setSourceColumn(dto.getSourceColumn());
        entity.setDisplayName(dto.getDisplayName());
        entity.setDataType(dto.getDataType());
        entity.setIsSortable(dto.getIsSortable());
        entity.setIsFilterable(dto.getIsFilterable());
        entity.setIsExportable(dto.getIsExportable());
        entity.setIsVisible(dto.getIsVisible());
        entity.setSortPriority(dto.getSortPriority());
        entity.setSortDirection(dto.getSortDirection());
        entity.setColumnWidth(dto.getColumnWidth());
        entity.setAlignment(dto.getAlignment());

        // Handle formatting JSON
        if (dto.getFormattingJson() != null) {
            try {
                Map<String, Object> formattingMap = objectMapper.convertValue(
                        dto.getFormattingJson(),
                        new TypeReference<Map<String, Object>>() {}
                );
                entity.setFormattingJson(formattingMap);
            } catch (Exception e) {
                log.error("Error converting formatting JSON for column: {}", e.getMessage());
            }
        }

        return entity;
    }

    public void updateColumnEntity(ReportColumnEntity entity, ReportColumnUpdateDto dto) {
        if (dto.getDisplayName() != null) {
            entity.setDisplayName(dto.getDisplayName());
        }
        if (dto.getIsSortable() != null) {
            entity.setIsSortable(dto.getIsSortable());
        }
        if (dto.getIsFilterable() != null) {
            entity.setIsFilterable(dto.getIsFilterable());
        }
        if (dto.getIsExportable() != null) {
            entity.setIsExportable(dto.getIsExportable());
        }
        if (dto.getIsVisible() != null) {
            entity.setIsVisible(dto.getIsVisible());
        }
        if (dto.getSortPriority() != null) {
            entity.setSortPriority(dto.getSortPriority());
        }
        if (dto.getSortDirection() != null) {
            entity.setSortDirection(dto.getSortDirection());
        }
        if (dto.getColumnWidth() != null) {
            entity.setColumnWidth(dto.getColumnWidth());
        }
        if (dto.getAlignment() != null) {
            entity.setAlignment(dto.getAlignment());
        }

        // Handle formatting JSON
        if (dto.getFormattingJson() != null) {
            try {
                Map<String, Object> formattingMap = objectMapper.convertValue(
                        dto.getFormattingJson(),
                        new TypeReference<Map<String, Object>>() {}
                );
                entity.setFormattingJson(formattingMap);
            } catch (Exception e) {
                log.error("Error converting formatting JSON for update: {}", e.getMessage());
            }
        }
    }


}