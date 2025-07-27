package com.dair.cais.cases.casealert;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Case-Alert association.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseAlert {
    private Long id;
    private Long caseId;
    private String alertId;
    private String addedBy;
    private LocalDateTime addedAt;
    private String reason;
}