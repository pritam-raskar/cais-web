package com.dair.cais.steps.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Dedicated service for async notification handling to avoid self-invocation issues
 */
@Service
@Slf4j
@RequiredArgsConstructor
@EnableRetry
public class AsyncNotificationService {
    
    private final StepNotificationService stepNotificationService;
    
    /**
     * Sends step change notifications asynchronously with retry logic
     */
    @Async("notificationExecutor")
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendStepChangeNotifications(String alertId, String previousStepName, String stepName, 
                                          String userId, long operationDurationMs, String deadline, 
                                          boolean shouldUpdateOwner, String assignedUserId) {
        try {
            Map<String, String> notificationContext = new HashMap<>();
            notificationContext.put("operationDurationMs", String.valueOf(operationDurationMs));
            notificationContext.put("deadline", deadline);
            if (shouldUpdateOwner && assignedUserId != null) {
                notificationContext.put("assignedTo", assignedUserId);
            }
            
            stepNotificationService.sendStepChangeNotification(alertId, previousStepName, stepName, userId, notificationContext);
            log.debug("Step change notification sent for alert: {}", alertId);
            
            // Send assignment notification if auto-assigned
            if (shouldUpdateOwner && assignedUserId != null) {
                stepNotificationService.sendStepAssignmentNotification(alertId, assignedUserId, stepName, userId);
                log.debug("Step assignment notification sent for alert: {} to user: {}", alertId, assignedUserId);
            }
            
        } catch (Exception e) {
            log.error("Failed to send step change notifications for alert: {} (attempt will be retried)", alertId, e);
            throw e; // Re-throw to trigger retry
        }
    }
    
    /**
     * Fallback method when all retry attempts fail
     */
    public void sendStepChangeNotificationsFallback(String alertId, String previousStepName, String stepName, 
                                                   String userId, long operationDurationMs, String deadline, 
                                                   boolean shouldUpdateOwner, String assignedUserId, Exception ex) {
        log.error("All retry attempts failed for step change notification. Alert: {}, Error: {}", alertId, ex.getMessage());
        
        // Could implement fallback logic here:
        // - Store failed notifications in database for later processing
        // - Send to dead letter queue
        // - Send admin alert
        
        // For now, just log the failure with all context
        log.warn("Failed notification context: alertId={}, step={}→{}, user={}, duration={}ms", 
                alertId, previousStepName, stepName, userId, operationDurationMs);
    }
}
