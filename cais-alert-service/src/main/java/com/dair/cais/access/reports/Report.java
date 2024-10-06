package com.dair.cais.access.reports;

import lombok.Data;

@Data
public class Report {
    private Integer reportId;
    private String reportName;
    private String reportDescription;
    private Integer connectorId;
    private String tableOrViewName;
}
