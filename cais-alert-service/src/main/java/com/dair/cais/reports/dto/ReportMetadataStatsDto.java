package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportMetadataStatsDto {
    private Long totalReports;
    private Long publishedReports;
    private Long draftReports;
    private Long archivedReports;
    private java.time.ZonedDateTime lastUpdated;
}
