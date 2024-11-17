package com.dair.cais.filter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserFilterUpdateDto {

    @Size(max = 255, message = "Filter name cannot exceed 255 characters")
    private String filterName;

    private String filterDescription;

    private Boolean isDefault;

    private Boolean isPublic;

    private String filterConfig;

    @Size(max = 100, message = "Entity identifier cannot exceed 100 characters")
    private String entityIdentifier;

    // Optional: Add validation annotations if needed
    private String sourceIdentifier;
}