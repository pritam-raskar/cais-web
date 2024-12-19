package com.dair.cais.reports.tabs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tab summary response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabSummaryDto {
    @Schema(description = "Tab ID")
    private Integer reportId;

    @Schema(description = "Tab name")
    private String tabName;

    @Schema(description = "Tab description")
    private String tabDescription;
}
