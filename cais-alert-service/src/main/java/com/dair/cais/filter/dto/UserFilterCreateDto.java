// File: com/dair/cais/filter/dto/UserFilterCreateDto.java
package com.dair.cais.filter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserFilterCreateDto {
    @NotNull(message = "Entity type ID is required")
    private Long entityTypeId;

    @NotBlank(message = "Entity identifier is required")
    private String entityIdentifier;

    @NotBlank(message = "Filter name is required")
    private String filterName;

    private String filterDescription;

    private Boolean isDefault = false;

    private Boolean isPublic = false;

    //@NotNull(message = "Filter configuration is required")
    private String filterConfig;
}
