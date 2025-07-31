package com.dair.cais.steps.assignment;

import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import com.dair.cais.access.PolicyEntityMapping.PolicyEntityMappingEntity;
import com.dair.cais.access.PolicyEntityMapping.PolicyEntityMappingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling automatic user assignment during step changes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StepAssignmentService {
    
    private final PolicyRepository policyRepository;
    private final PolicyEntityMappingRepository policyEntityMappingRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Gets assigned user for a step based on assignment rules
     * @param stepId Target step ID
     * @param orgUnitId Organization unit ID
     * @param alertId Alert ID for context
     * @return User ID to assign, or null if no assignment rule
     */
    public String getAssignedUser(Long stepId, String orgUnitId, String alertId) {
        log.debug("Getting assigned user for step: {} in org: {}", stepId, orgUnitId);
        
        try {
            // Find step assignment policies
            List<PolicyEntity> assignmentPolicies = policyRepository.findByTypeAndIsActive("STEP_ASSIGNMENT", true);
            
            for (PolicyEntity policy : assignmentPolicies) {
                String assignedUser = evaluateAssignmentPolicy(policy, stepId, orgUnitId, alertId);
                if (StringUtils.hasText(assignedUser)) {
                    log.debug("Found assignment rule: {} → user: {}", stepId, assignedUser);
                    return assignedUser;
                }
            }
            
            // Fallback to entity mapping
            return getAssignmentFromEntityMapping(stepId, orgUnitId);
            
        } catch (Exception e) {
            log.error("Error getting assigned user for step: {}", stepId, e);
            return null;
        }
    }
    
    /**
     * Evaluates assignment policy for step/org combination
     */
    private String evaluateAssignmentPolicy(PolicyEntity policy, Long stepId, String orgUnitId, String alertId) {
        try {
            if (!StringUtils.hasText(policy.getDescription())) {
                return null;
            }
            
            JsonNode policyConfig = objectMapper.readTree(policy.getDescription());
            
            if (!policyConfig.has("assignments")) {
                return null;
            }
            
            JsonNode assignments = policyConfig.get("assignments");
            
            for (JsonNode assignment : assignments) {
                if (matchesAssignmentCriteria(assignment, stepId, orgUnitId, alertId)) {
                    return extractAssignedUser(assignment);
                }
            }
            
        } catch (Exception e) {
            log.warn("Error evaluating assignment policy: {}", policy.getName(), e);
        }
        
        return null;
    }
    
    /**
     * Checks if assignment criteria matches
     */
    private boolean matchesAssignmentCriteria(JsonNode assignment, Long stepId, String orgUnitId, String alertId) {
        // Check step match
        if (assignment.has("stepId")) {
            Long assignmentStepId = assignment.get("stepId").asLong();
            if (!assignmentStepId.equals(stepId)) {
                return false;
            }
        }
        
        // Check org unit match
        if (assignment.has("orgUnitId")) {
            String assignmentOrgId = assignment.get("orgUnitId").asText();
            if (!assignmentOrgId.equals(orgUnitId)) {
                return false;
            }
        }
        
        // Check step type/category
        if (assignment.has("stepType")) {
            // Would need step type lookup - placeholder for now
            log.debug("Step type matching not implemented yet");
        }
        
        return true;
    }
    
    /**
     * Extracts assigned user from assignment configuration
     */
    private String extractAssignedUser(JsonNode assignment) {
        if (assignment.has("assignTo")) {
            JsonNode assignTo = assignment.get("assignTo");
            
            if (assignTo.has("userId")) {
                return assignTo.get("userId").asText();
            }
            
            if (assignTo.has("role")) {
                String roleName = assignTo.get("role").asText();
                // Would need to find user with role in org - placeholder
                log.debug("Role-based assignment not fully implemented: {}", roleName);
                return null;
            }
            
            if (assignTo.has("queue")) {
                String queueName = assignTo.get("queue").asText();
                // Would need queue management - placeholder
                log.debug("Queue-based assignment not implemented: {}", queueName);
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Gets assignment from policy entity mapping as fallback
     */
    private String getAssignmentFromEntityMapping(Long stepId, String orgUnitId) {
        try {
            List<PolicyEntityMappingEntity> mappings = policyEntityMappingRepository
                    .findByEntityTypeAndEntityId("STEP", stepId.toString());
            
            for (PolicyEntityMappingEntity mapping : mappings) {
                // Check if mapping has assignment info in condition
                if (StringUtils.hasText(mapping.getCondition())) {
                    try {
                        JsonNode condition = objectMapper.readTree(mapping.getCondition());
                        if (condition.has("assignTo") && condition.has("orgUnitId")) {
                            String mappingOrgId = condition.get("orgUnitId").asText();
                            if (orgUnitId.equals(mappingOrgId)) {
                                return condition.get("assignTo").asText();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing entity mapping condition", e);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error getting assignment from entity mapping", e);
        }
        
        return null;
    }
    
    /**
     * Creates assignment rule for step/org combination
     */
    public void createAssignmentRule(Long stepId, String orgUnitId, String assignToUserId, String createdBy) {
        log.debug("Creating assignment rule: step {} in org {} → user {}", stepId, orgUnitId, assignToUserId);
        
        try {
            PolicyEntityMappingEntity mapping = new PolicyEntityMappingEntity();
            mapping.setEntityType("STEP");
            mapping.setEntityId(stepId.toString());
            // TODO: Set action entity instead of actionId
            // ActionEntity assignAction = actionRepository.findById(1).orElse(null);
            // mapping.setAction(assignAction);
            
            // Create condition with assignment info
            String condition = String.format(
                "{\"orgUnitId\":\"%s\",\"assignTo\":\"%s\",\"createdBy\":\"%s\"}", 
                orgUnitId, assignToUserId, createdBy);
            mapping.setCondition(condition);
            
            policyEntityMappingRepository.save(mapping);
            log.info("Created assignment rule for step: {}", stepId);
            
        } catch (Exception e) {
            log.error("Error creating assignment rule", e);
        }
    }
}
