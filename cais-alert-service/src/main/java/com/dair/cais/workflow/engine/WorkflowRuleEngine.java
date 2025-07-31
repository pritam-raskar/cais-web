package com.dair.cais.workflow.engine;

import com.dair.cais.alert.Alert;
import com.dair.cais.workflow.entity.WorkflowTransitionEntity;
import com.dair.cais.workflow.repository.WorkflowTransitionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Workflow Rule Engine for validating business rules and prerequisites
 * before step transitions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowRuleEngine {
    
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Validates all business rules for a step transition
     * @param workflowId The workflow ID
     * @param currentStepId Current step ID
     * @param targetStepId Target step ID
     * @param alert Alert data for rule evaluation
     * @return ValidationResult with success status and error messages
     */
    public ValidationResult validateTransitionRules(Long workflowId, Long currentStepId, Long targetStepId, Alert alert) {
        log.debug("Validating transition rules for workflow: {} from step: {} to step: {}", 
                 workflowId, currentStepId, targetStepId);
        
        List<String> errors = new ArrayList<>();
        
        try {
            // Get transition with features
            List<WorkflowTransitionEntity> transitions = workflowTransitionRepository
                    .findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(workflowId, currentStepId);
            
            WorkflowTransitionEntity targetTransition = transitions.stream()
                    .filter(t -> targetStepId.equals(t.getTargetStep().getStep().getStepId()))
                    .findFirst()
                    .orElse(null);
            
            if (targetTransition == null) {
                errors.add("No valid transition found from step " + currentStepId + " to step " + targetStepId);
                return new ValidationResult(false, errors);
            }
            
            // TODO: Implement transition features/rules validation when WorkflowTransitionFeatureEntity is created
            
            boolean isValid = errors.isEmpty();
            log.debug("Transition rule validation result: {} for workflow: {}", isValid, workflowId);
            
            return new ValidationResult(isValid, errors);
            
        } catch (Exception e) {
            log.error("Error validating transition rules for workflow: {}", workflowId, e);
            errors.add("Error validating transition rules: " + e.getMessage());
            return new ValidationResult(false, errors);
        }
    }
    
    
    /**
     * Validates JSON-based rules
     */
    private ValidationResult validateJsonRule(String jsonRule, Alert alert) {
        List<String> errors = new ArrayList<>();
        
        try {
            JsonNode rule = objectMapper.readTree(jsonRule);
            
            if (rule.has("requiredFields")) {
                ValidationResult fieldResult = validateRequiredFields(rule.get("requiredFields"), alert);
                if (!fieldResult.isValid()) {
                    errors.addAll(fieldResult.getErrors());
                }
            }
            
            if (rule.has("minScore")) {
                int minScore = rule.get("minScore").asInt();
                if (alert.getTotalScore() == null || alert.getTotalScore().doubleValue() < minScore) {
                    errors.add("Alert score " + alert.getTotalScore() + " is below required minimum: " + minScore);
                }
            }
            
            if (rule.has("requiredStatus")) {
                String requiredStatus = rule.get("requiredStatus").asText();
                if (!requiredStatus.equals(alert.getStatus())) {
                    errors.add("Alert status must be: " + requiredStatus);
                }
            }
            
        } catch (Exception e) {
            log.error("Error parsing JSON rule: {}", jsonRule, e);
            errors.add("Invalid JSON rule format");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates required fields rule
     */
    private ValidationResult validateRequiredFields(JsonNode requiredFields, Alert alert) {
        List<String> errors = new ArrayList<>();
        
        requiredFields.forEach(field -> {
            String fieldName = field.asText();
            switch (fieldName) {
                case "ownerId":
                    if (!StringUtils.hasText(alert.getOwnerId())) {
                        errors.add("Owner is required for this transition");
                    }
                    break;
                case "reason":
                    if (!StringUtils.hasText(alert.getReasonDetails() != null ? alert.getReasonDetails().toString() : null)) {
                        errors.add("Reason is required for this transition");
                    }
                    break;
                case "customerName":
                    if (!StringUtils.hasText(alert.getCustomerName())) {
                        errors.add("Customer name is required for this transition");
                    }
                    break;
            }
        });
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates total score requirement
     */
    private ValidationResult validateTotalScoreRule(Alert alert) {
        List<String> errors = new ArrayList<>();
        
        if (alert.getTotalScore() == null || alert.getTotalScore().doubleValue() <= 0) {
            errors.add("Total score is required and must be greater than 0");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates owner requirement
     */
    private ValidationResult validateOwnerRule(Alert alert) {
        List<String> errors = new ArrayList<>();
        
        if (!StringUtils.hasText(alert.getOwnerId())) {
            errors.add("Alert must be assigned to an owner");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates high priority requirement
     */
    private ValidationResult validateHighPriorityRule(Alert alert) {
        List<String> errors = new ArrayList<>();
        
        if (!"HIGH".equalsIgnoreCase(alert.getPriority())) {
            errors.add("Alert must be high priority for this transition");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates checklist completion requirement
     */
    private ValidationResult validateChecklistCompletion(Alert alert) {
        List<String> errors = new ArrayList<>();
        
        // This would require integration with checklist service
        // For now, we'll check if alert has completed basic requirements
        if (!StringUtils.hasText(alert.getReasonDetails() != null ? alert.getReasonDetails().toString() : null)) {
            errors.add("Investigation checklist must be completed before this transition");
        }
        
        if (!StringUtils.hasText(alert.getOwnerId())) {
            errors.add("Alert must be assigned before checklist can be completed");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates attachments requirement
     */
    private ValidationResult validateAttachmentsRule(Alert alert) {
        List<String> errors = new ArrayList<>();
        
        // This would require checking attachment count from attachment service
        // For now, we'll assume this validation is implemented elsewhere
        log.debug("Validating attachments requirement for alert: {}", alert.getAlertId());
        
        // Placeholder validation - in real implementation, would query attachment service
        // if (attachmentService.getAttachmentCount(alert.getAlertId()) == 0) {
        //     errors.add("At least one attachment is required for this transition");
        // }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates notes requirement
     */
    private ValidationResult validateNotesRule(Alert alert) {
        List<String> errors = new ArrayList<>();
        
        // This would require checking notes count from note service
        // For now, we'll assume this validation is implemented elsewhere
        log.debug("Validating notes requirement for alert: {}", alert.getAlertId());
        
        // Placeholder validation - in real implementation, would query note service
        // if (noteService.getNoteCount(alert.getAlertId()) == 0) {
        //     errors.add("At least one note is required for this transition");
        // }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validates reason details requirement
     */
    private ValidationResult validateReasonDetailsRule(Alert alert) {
        List<String> errors = new ArrayList<>();
        
        if (alert.getReasonDetails() == null || 
            !StringUtils.hasText(alert.getReasonDetails().toString())) {
            errors.add("Detailed reason is required for this transition");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Result class for validation operations
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
