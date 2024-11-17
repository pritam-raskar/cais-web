package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportExecutionResultDto {
    private Integer reportId;
    private String reportName;
    private List<ReportColumnDto> columns;
    private List<Map<String, Object>> data;
    private Integer totalRows;
    private Integer pageSize;
    private Integer currentPage;
    private Integer totalPages;
    private Boolean isCached;
    private ZonedDateTime executionTime;
    private ExecutionMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionMetadata {
        private Long executionTimeMs;
        private String queryString;
        private Integer returnedRows;
        private Integer filteredRows;
        private ZonedDateTime cacheExpiry;
    }
}