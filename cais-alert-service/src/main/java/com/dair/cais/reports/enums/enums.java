package com.dair.cais.reports.enums;

public class enums {

    public enum ReportType {
        TABULAR,
        PIVOT,
        CHART
    }

    public enum ReportStatus {
        DRAFT,
        ACTIVE,
        ARCHIVED
    }

    public enum ColumnAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum ParameterType {
        TEXT,          // For text/string inputs
        NUMBER,        // For numeric inputs
        DATE,          // For single date selection
        DATETIME,      // For date with time selection
        DATE_RANGE,    // For date range selection
        BOOLEAN,       // For true/false inputs
        LIST,          // For single select from a list
        MULTI_SELECT   // For multiple select from a list
    }

}
