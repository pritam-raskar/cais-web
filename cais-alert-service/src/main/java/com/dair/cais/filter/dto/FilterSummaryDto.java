package com.dair.cais.filter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.sql.Timestamp;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterSummaryDto {
    private Long filterId;
    private String filterName;
    private String filterDescription;
    private Boolean isDefault;
    private Boolean isPublic;
    private String entityName;
    private Timestamp updatedAt;
    private String updatedBy;
}