package com.dair.cais.alert.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BulkStepChangeResponse {
    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<String> successfulAlertIds;
    private Map<String, String> failedAlerts; // alertId -> error message
    private List<String> validationErrors;
}
