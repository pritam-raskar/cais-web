package com.dair.cais.organization;

import lombok.Data;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

@Data
public class OrganizationUnit {
    private Integer orgId;
    private String type;
    
    @NotBlank(message = "Organization key is required")
    private String orgKey;
    
    private String orgName;
    private String orgDescription;
    private Boolean isActive;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
