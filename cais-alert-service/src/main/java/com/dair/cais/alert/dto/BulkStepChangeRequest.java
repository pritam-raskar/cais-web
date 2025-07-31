package com.dair.cais.alert.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BulkStepChangeRequest {
    @NotEmpty(message = "Alert IDs cannot be empty")
    private List<String> alertIds;
    
    @NotNull(message = "Step ID is required")
    private Long stepId;
    
    private String reason;
    private boolean skipValidation = false;
}
