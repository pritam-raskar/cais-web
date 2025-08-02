package com.dair.cais.cases.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BulkStepChangeRequest {
    @NotEmpty(message = "Case IDs cannot be empty")
    private List<Long> caseIds;
    
    @NotNull(message = "Step ID is required")
    private Long stepId;
    
    private String reason;
    private boolean skipValidation = false;
}