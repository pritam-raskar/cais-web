package com.dair.cais.filter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.sql.Timestamp;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterResponseDto {
    private Long filterId;
    private String userId;
    private String entityIdentifier;
    private String sourceIdentifier;
    private String filterName;
    private String filterDescription;
    private Boolean isDefault;
    private Boolean isPublic;
    private String filterConfig;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String createdBy;
    private String updatedBy;
}