package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Full Column DTO including all fields
 */
@Data
@Builder(access = AccessLevel.PUBLIC) // This makes builder methods public
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportColumnDto extends BaseAuditDto {
    private Integer crcId;
    private Integer reportId;
    private String sourceColumn;
    private String displayName;
    private String dataType;
    private Boolean isSortable;
    private Boolean isFilterable;
    private Boolean isExportable;
    private Boolean isVisible;
    private Integer sortPriority;
    private String sortDirection;
    private String columnWidth;
    private String alignment;
    private FormattingConfigDto formattingJson;

    public static ReportColumnDtoBuilder builder() {
        return new ReportColumnDtoBuilder();
    }

    public static class ReportColumnDtoBuilder {
        private Integer crcId;
        private Integer reportId;
        private String sourceColumn;
        private String displayName;
        private String dataType;
        private Boolean isSortable;
        private Boolean isFilterable;
        private Boolean isExportable;
        private Boolean isVisible;
        private Integer sortPriority;
        private String sortDirection;
        private String columnWidth;
        private String alignment;
        private FormattingConfigDto formattingJson;

        ReportColumnDtoBuilder() {
        }

        public ReportColumnDtoBuilder crcId(Integer crcId) {
            this.crcId = crcId;
            return this;
        }

        public ReportColumnDtoBuilder reportId(Integer reportId) {
            this.reportId = reportId;
            return this;
        }

        // ... similar methods for other fields ...

        public ReportColumnDto build() {
            ReportColumnDto dto = new ReportColumnDto();
            dto.setCrcId(this.crcId);
            dto.setReportId(this.reportId);
            dto.setSourceColumn(this.sourceColumn);
            dto.setDisplayName(this.displayName);
            dto.setDataType(this.dataType);
            dto.setIsSortable(this.isSortable);
            dto.setIsFilterable(this.isFilterable);
            dto.setIsExportable(this.isExportable);
            dto.setIsVisible(this.isVisible);
            dto.setSortPriority(this.sortPriority);
            dto.setSortDirection(this.sortDirection);
            dto.setColumnWidth(this.columnWidth);
            dto.setAlignment(this.alignment);
            dto.setFormattingJson(this.formattingJson);
            return dto;
        }
    }
}