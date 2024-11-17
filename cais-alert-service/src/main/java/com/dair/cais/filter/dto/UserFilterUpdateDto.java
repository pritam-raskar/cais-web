package com.dair.cais.filter.dto;

import lombok.Data;

@Data
public class UserFilterUpdateDto {
    private String filterName;
    private String filterDescription;
    private Boolean isDefault;
    private Boolean isPublic;

    //@NotNull(message = "Filter configuration is required")
    private String filterConfig;
}
