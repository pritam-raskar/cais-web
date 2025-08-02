package com.dair.cais.cases.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BulkStepChangeResponse {
    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<Long> successfulCaseIds;
    private Map<Long, String> failedCases; // caseId -> error message
    private List<String> validationErrors;
}