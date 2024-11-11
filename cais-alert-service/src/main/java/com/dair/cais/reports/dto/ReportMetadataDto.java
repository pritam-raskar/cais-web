package com.dair.cais.reports.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO for report metadata
 */
@Data
public class ReportMetadataDto {
    private List<TableMetadataDto> availableTables;
    private List<String> availableFormats;
    private Map<String, String> defaultDataTypeFormats;
}