package com.dair.cais.filter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserFilterCreateDto {
    @NotNull(message = "Entity type ID is required")
    private Long entityTypeId;

    @NotBlank(message = "Entity identifier is required")
    private String entityIdentifier;

    @NotBlank(message = "Filter name is required")
    @Size(max = 255, message = "Filter name cannot exceed 255 characters")
    private String filterName;

    private String filterDescription;

    private Boolean isDefault = false;

    private Boolean isPublic = false;

    private String filterConfig;

    private String sourceIdentifier;
}