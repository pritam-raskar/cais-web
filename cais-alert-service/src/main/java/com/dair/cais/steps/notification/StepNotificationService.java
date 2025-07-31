package com.dair.cais.steps.notification;

import com.dair.cais.communication.CommunicationEntity;
import com.dair.cais.communication.CommunicationRepository;
// TODO: Add EnvironmentConfigRepository when implemented
// import com.dair.cais.common.config.EnvironmentConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling step change notifications
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StepNotificationService {
    
    private final CommunicationRepository communicationRepository;
    // TODO: Add EnvironmentConfigRepository when implemented
    // private final EnvironmentConfigRepository environmentConfigRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Sends step change notification
     * @param alertId Alert ID
     * @param fromStepName Previous step name
     * @param toStepName New step name
     * @param userId User who initiated the change
     * @param additionalInfo Additional context information
     */
    public void sendStepChangeNotification(String alertId, String fromStepName, String toStepName, 
                                         String userId, Map<String, String> additionalInfo) {
        log.debug("Sending step change notification for alert: {} from {} to {}", alertId, fromStepName, toStepName);
        
        try {
            // Get notification template
            String template = getNotificationTemplate("STEP_CHANGE");
            if (!StringUtils.hasText(template)) {
                log.warn("No notification template found for STEP_CHANGE, using default");
                template = getDefaultStepChangeMessage();
            }
            
            // Build notification message
            String message = buildNotificationMessage(template, alertId, fromStepName, toStepName, userId, additionalInfo);
            
            // Create communication record
            CommunicationEntity communication = new CommunicationEntity();
            communication.setAlertId(alertId);
            communication.setType("STEP_CHANGE_NOTIFICATION");
            communication.setMessage(message);
            communication.setUserId(userId);
            communication.setCreateDate(LocalDateTime.now());
            communication.setHasAttachment(false);
            
            // Save notification
            communicationRepository.save(communication);
            
            log.debug("Step change notification sent successfully for alert: {}", alertId);
            
        } catch (Exception e) {
            log.error("Error sending step change notification for alert: {}", alertId, e);
        }
    }
    
    /**
     * Sends step transition validation failure notification
     */
    public void sendValidationFailureNotification(String alertId, String userId, String errorMessage) {
        log.debug("Sending validation failure notification for alert: {}", alertId);
        
        try {
            String template = getNotificationTemplate("VALIDATION_FAILURE");
            if (!StringUtils.hasText(template)) {
                template = "Step transition validation failed for alert {alertId}: {errorMessage}";
            }
            
            String message = template
                    .replace("{alertId}", alertId)
                    .replace("{errorMessage}", errorMessage)
                    .replace("{userId}", userId);
            
            CommunicationEntity communication = new CommunicationEntity();
            communication.setAlertId(alertId);
            communication.setType("VALIDATION_FAILURE");
            communication.setMessage(message);
            communication.setUserId(userId);
            communication.setCreateDate(LocalDateTime.now());
            communication.setHasAttachment(false);
            
            communicationRepository.save(communication);
            
        } catch (Exception e) {
            log.error("Error sending validation failure notification", e);
        }
    }
    
    /**
     * Sends step assignment notification when alert gets auto-assigned
     */
    public void sendStepAssignmentNotification(String alertId, String assignedUserId, String stepName, String assignedBy) {
        log.debug("Sending step assignment notification for alert: {} to user: {}", alertId, assignedUserId);
        
        try {
            String template = getNotificationTemplate("STEP_ASSIGNMENT");
            if (!StringUtils.hasText(template)) {
                template = "Alert {alertId} has been assigned to you at step {stepName} by {assignedBy}";
            }
            
            String message = template
                    .replace("{alertId}", alertId)
                    .replace("{assignedUserId}", assignedUserId)
                    .replace("{stepName}", stepName)
                    .replace("{assignedBy}", assignedBy);
            
            CommunicationEntity communication = new CommunicationEntity();
            communication.setAlertId(alertId);
            communication.setType("STEP_ASSIGNMENT");
            communication.setMessage(message);
            communication.setUserId(assignedUserId);
            communication.setCreateDate(LocalDateTime.now());
            communication.setHasAttachment(false);
            
            communicationRepository.save(communication);
            
        } catch (Exception e) {
            log.error("Error sending step assignment notification", e);
        }
    }
    
    /**
     * Gets notification template from environment config
     */
    private String getNotificationTemplate(String templateType) {
        try {
            String configName = "NOTIFICATION_TEMPLATE_" + templateType;
            // TODO: Implement EnvironmentConfigRepository
            // return environmentConfigRepository.findByName(configName)
            //         .map(config -> config.getValue())
            //         .orElse(null);
            return null; // Temporary placeholder
        } catch (Exception e) {
            log.error("Error retrieving notification template: {}", templateType, e);
            return null;
        }
    }
    
    /**
     * Builds notification message from template
     */
    private String buildNotificationMessage(String template, String alertId, String fromStepName, 
                                          String toStepName, String userId, Map<String, String> additionalInfo) {
        String message = template
                .replace("{alertId}", alertId)
                .replace("{fromStep}", fromStepName != null ? fromStepName : "Initial")
                .replace("{toStep}", toStepName)
                .replace("{userId}", userId)
                .replace("{timestamp}", LocalDateTime.now().toString());
        
        // Replace additional placeholders
        if (additionalInfo != null) {
            for (Map.Entry<String, String> entry : additionalInfo.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message;
    }
    
    /**
     * Default step change message template
     */
    private String getDefaultStepChangeMessage() {
        return "Alert {alertId} has been moved from {fromStep} to {toStep} by user {userId} at {timestamp}";
    }
    
    /**
     * Stores notification template in environment config
     */
    public void saveNotificationTemplate(String templateType, String template) {
        log.debug("Saving notification template for type: {}", templateType);
        
        try {
            String configName = "NOTIFICATION_TEMPLATE_" + templateType;
            
            // This would require implementing the save functionality in EnvironmentConfigRepository
            // For now, log that the template would be saved
            log.info("Would save notification template '{}' with value: {}", configName, template);
            
        } catch (Exception e) {
            log.error("Error saving notification template: {}", templateType, e);
        }
    }
}
