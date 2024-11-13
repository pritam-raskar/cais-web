package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportListResponseDto {
    private List<ReportSummaryDto> reports;
    private ReportMetadataStatsDto metadata;
}

