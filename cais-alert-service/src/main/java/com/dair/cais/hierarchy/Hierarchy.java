package com.dair.cais.hierarchy;

import lombok.Data;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;

@Data
public class Hierarchy {
    private Integer hierarchyId;

    @NotBlank(message = "Hierarchy key is required")
    @Size(max = 255, message = "Hierarchy key must not exceed 255 characters")
    private String hierarchyKey;

    @NotBlank(message = "Hierarchy name is required")
    @Size(max = 255, message = "Hierarchy name must not exceed 255 characters")
    private String hierarchyName;

    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
