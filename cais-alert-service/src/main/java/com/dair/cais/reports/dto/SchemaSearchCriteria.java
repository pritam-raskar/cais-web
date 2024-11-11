package com.dair.cais.reports.dto;

import lombok.Data;

/**
 * DTO for schema search criteria
 */
@Data
public class SchemaSearchCriteria {
    private String searchTerm;
    private Boolean includeSystemSchemas = false;
    private Boolean includeTables = true;
    private Boolean includeViews = true;
    private Boolean includeCounts = true;
}
