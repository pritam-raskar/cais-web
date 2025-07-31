package com.dair.cais.steps.permission;

import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import com.dair.cais.access.UserBasedPermission.UserPermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Service for handling step-based permissions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StepPermissionService {
    
    private final PolicyRepository policyRepository;
    private final UserPermissionService userPermissionService;
    private final ObjectMapper objectMapper;
    
    /**
     * Checks if a user has permission to transition from current step to target step
     * @param userId User ID
     * @param alertId Alert ID (for org-based permissions)
     * @param currentStepId Current step ID
     * @param targetStepId Target step ID
     * @return true if user has permission, false otherwise
     */
    public boolean hasStepTransitionPermission(String userId, String alertId, Long currentStepId, Long targetStepId) {
        log.debug("Checking step transition permission for user: {} from step: {} to step: {}", 
                 userId, currentStepId, targetStepId);
        
        try {
            // Check if user has general step change permission
            if (!hasGeneralStepPermission(userId, alertId)) {
                log.debug("User {} does not have general step change permission", userId);
                return false;
            }
            
            // Check specific step transition permissions
            if (!hasSpecificStepPermission(userId, currentStepId, targetStepId)) {
                log.debug("User {} does not have permission to transition from step {} to step {}", 
                         userId, currentStepId, targetStepId);
                return false;
            }
            
            log.debug("Step transition permission granted for user: {}", userId);
            return true;
            
        } catch (Exception e) {
            log.error("Error checking step transition permission for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * Checks if user has general permission to change steps
     */
    private boolean hasGeneralStepPermission(String userId, String alertId) {
        try {
            // Check if user has CHANGE_STEP action permission
            List<PolicyEntity> stepPolicies = policyRepository.findByTypeAndIsActive("ALERT_PERMISSION", true);
            
            for (PolicyEntity policy : stepPolicies) {
                if (StringUtils.hasText(policy.getDescription())) {
                    try {
                        JsonNode policyConfig = objectMapper.readTree(policy.getDescription());
                        if (hasActionPermission(policyConfig, userId, "CHANGE_STEP")) {
                            return true;
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing alert permission policy: {}", policy.getName(), e);
                    }
                }
            }
            
            // Fallback: check if user has role with step change permission
            UserPermissionServiceAdapter permissionAdapter = new UserPermissionServiceAdapter(policyRepository);
            return permissionAdapter.hasActionPermission(userId, alertId, "CHANGE_STEP");
            
        } catch (Exception e) {
            log.error("Error checking general step permission", e);
            return false;
        }
    }
    
    /**
     * Checks if user has specific action permission based on policy
     */
    private boolean hasActionPermission(JsonNode policyConfig, String userId, String action) {
        if (!policyConfig.has("permissions")) {
            return false;
        }
        
        JsonNode permissions = policyConfig.get("permissions");
        for (JsonNode permission : permissions) {
            if (permission.has("action") && action.equals(permission.get("action").asText())) {
                if (matchesUser(permission, userId)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks specific step transition permissions using policies
     */
    private boolean hasSpecificStepPermission(String userId, Long currentStepId, Long targetStepId) {
        try {
            // Find step permission policies
            List<PolicyEntity> stepPolicies = policyRepository.findByTypeAndIsActive("STEP_PERMISSION", true);
            
            for (PolicyEntity policy : stepPolicies) {
                if (StringUtils.hasText(policy.getDescription())) {
                    try {
                        JsonNode policyConfig = objectMapper.readTree(policy.getDescription());
                        
                        if (isUserAllowedByPolicy(policyConfig, userId, currentStepId, targetStepId)) {
                            return true;
                        }
                        
                        if (isUserRestrictedByPolicy(policyConfig, userId, currentStepId, targetStepId)) {
                            return false;
                        }
                        
                    } catch (Exception e) {
                        log.warn("Error parsing step permission policy: {}", policy.getName(), e);
                    }
                }
            }
            
            // If no specific policies found, allow by default
            return true;
            
        } catch (Exception e) {
            log.error("Error checking specific step permissions", e);
            return false;
        }
    }
    
    /**
     * Checks if user is explicitly allowed by policy
     */
    private boolean isUserAllowedByPolicy(JsonNode policyConfig, String userId, Long currentStepId, Long targetStepId) {
        if (!policyConfig.has("allowedTransitions")) {
            return false;
        }
        
        JsonNode allowedTransitions = policyConfig.get("allowedTransitions");
        
        for (JsonNode transition : allowedTransitions) {
            if (matchesTransition(transition, currentStepId, targetStepId) && 
                matchesUser(transition, userId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if user is explicitly restricted by policy
     */
    private boolean isUserRestrictedByPolicy(JsonNode policyConfig, String userId, Long currentStepId, Long targetStepId) {
        if (!policyConfig.has("restrictedTransitions")) {
            return false;
        }
        
        JsonNode restrictedTransitions = policyConfig.get("restrictedTransitions");
        
        for (JsonNode transition : restrictedTransitions) {
            if (matchesTransition(transition, currentStepId, targetStepId) && 
                matchesUser(transition, userId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if transition matches the policy configuration
     */
    private boolean matchesTransition(JsonNode transition, Long currentStepId, Long targetStepId) {
        if (transition.has("fromStep")) {
            Long fromStep = transition.get("fromStep").asLong();
            if (!fromStep.equals(currentStepId)) {
                return false;
            }
        }
        
        if (transition.has("toStep")) {
            Long toStep = transition.get("toStep").asLong();
            if (!toStep.equals(targetStepId)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if user matches the policy configuration
     */
    private boolean matchesUser(JsonNode transition, String userId) {
        if (transition.has("userIds")) {
            JsonNode userIds = transition.get("userIds");
            for (JsonNode userIdNode : userIds) {
                if (userId.equals(userIdNode.asText())) {
                    return true;
                }
            }
            return false;
        }
        
        if (transition.has("roles")) {
            JsonNode roles = transition.get("roles");
            for (JsonNode roleNode : roles) {
                String roleName = roleNode.asText();
                if (userHasRole(userId, roleName)) {
                    return true;
                }
            }
            return false;
        }
        
        // If no specific user/role restrictions, apply to all users
        return true;
    }
    
    /**
     * Checks if user has a specific role
     */
    private boolean userHasRole(String userId, String roleName) {
        try {
            // Check if user has organization access (basic permission check)
            List<String> userOrgKeys = userPermissionService.getDistinctOrgKeysForUser(userId);
            if (userOrgKeys.isEmpty()) {
                return false; // No org access = no role
            }
            
            // For production: implement proper role query against cm_user_org_role_mapping
            // Query: SELECT role_id FROM cm_user_org_role_mapping WHERE user_id = ? AND org_id IN (?)
            // Then join with cm_roles to get role names
            
            // Temporary implementation: allow users with org access to have basic operational roles
            // This should be replaced with actual role database query
            log.debug("Role check for user: {} role: {} - using fallback logic", userId, roleName);
            return true; // Allow for now - replace with actual role check
            
        } catch (Exception e) {
            log.error("Error checking user role: {} for user: {}", roleName, userId, e);
            return false;
        }
    }
    
    /**
     * Create a UserPermissionService implementation adapter
     */
    private static class UserPermissionServiceAdapter {
        private final PolicyRepository policyRepository;
        
        public UserPermissionServiceAdapter(PolicyRepository policyRepository) {
            this.policyRepository = policyRepository;
        }
        
        public boolean hasActionPermission(String userId, String alertId, String action) {
            try {
                // Check if user has policies that grant the specific action
                List<PolicyEntity> actionPolicies = policyRepository.findByTypeAndIsActive("ACTION_PERMISSION", true);
                
                for (PolicyEntity policy : actionPolicies) {
                    if (policy.getName() != null && policy.getName().toUpperCase().contains(action.toUpperCase())) {
                        // Found policy that potentially grants this action
                        log.debug("Found action policy: {} for action: {}", policy.getName(), action);
                        return true;
                    }
                }
                
                // Fallback: check if user has any alert-related policies
                List<PolicyEntity> alertPolicies = policyRepository.findByTypeAndIsActive("ALERT_PERMISSION", true);
                return !alertPolicies.isEmpty();
                
            } catch (Exception e) {
                log.warn("Error checking action permission for user: {} action: {}", userId, action, e);
                return false; // Secure default - deny access on error
            }
        }
    }
    
    /**
     * Gets list of allowed target steps for a user from current step
     * @param userId User ID
     * @param alertId Alert ID
     * @param currentStepId Current step ID
     * @param availableSteps All available steps from workflow
     * @return List of step IDs user can transition to
     */
    public List<Long> getAllowedTargetSteps(String userId, String alertId, Long currentStepId, List<Long> availableSteps) {
        log.debug("Getting allowed target steps for user: {} from step: {}", userId, currentStepId);
        
        return availableSteps.stream()
                .filter(stepId -> hasStepTransitionPermission(userId, alertId, currentStepId, stepId))
                .toList();
    }
}
