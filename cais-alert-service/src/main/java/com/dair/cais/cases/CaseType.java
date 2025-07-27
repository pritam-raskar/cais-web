package com.dair.cais.cases;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Data Transfer Object for Case Type.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseType {
    private Long typeId;
    private String name;
    private String description;
    private Long workflowId;
    private Boolean isActive;
}