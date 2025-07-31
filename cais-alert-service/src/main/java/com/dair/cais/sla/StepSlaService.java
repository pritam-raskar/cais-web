package com.dair.cais.sla;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StepSlaService {
    
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String SLA_CONFIG_KEY = "step_sla_configuration";
    
    /**
     * Calculate deadline for a step based on SLA rules
     */
    public LocalDateTime calculateStepDeadline(Long stepId, String alertTypeId) {
        log.debug("Calculating deadline for stepId: {} and alertTypeId: {}", stepId, alertTypeId);
        
        try {
            JsonNode slaConfig = getSlaConfiguration();
            
            // Look for step-specific SLA first
            String stepSlaKey = "step_" + stepId;
            if (slaConfig.has(stepSlaKey)) {
                int hoursToAdd = slaConfig.get(stepSlaKey).get("slaHours").asInt();
                return calculateBusinessDeadline(LocalDateTime.now(), hoursToAdd);
            }
            
            // Fall back to alert type SLA
            String alertTypeSlaKey = "alertType_" + alertTypeId;
            if (slaConfig.has(alertTypeSlaKey)) {
                int hoursToAdd = slaConfig.get(alertTypeSlaKey).get("defaultStepSlaHours").asInt();
                return calculateBusinessDeadline(LocalDateTime.now(), hoursToAdd);
            }
            
            // Default SLA if no specific rules found
            int defaultHours = slaConfig.has("defaultSlaHours") ? 
                slaConfig.get("defaultSlaHours").asInt() : 72;
            return calculateBusinessDeadline(LocalDateTime.now(), defaultHours);
            
        } catch (Exception e) {
            log.error("Error calculating step deadline for stepId: {}", stepId, e);
            // Return default 72-hour deadline as fallback
            return LocalDateTime.now().plusHours(72);
        }
    }
    
    /**
     * Check if step is approaching SLA violation
     */
    public boolean isApproachingSlaViolation(LocalDateTime deadline) {
        if (deadline == null) return false;
        
        LocalDateTime warningThreshold = deadline.minusHours(4); // 4 hours before deadline
        return LocalDateTime.now().isAfter(warningThreshold);
    }
    
    /**
     * Check if step has violated SLA
     */
    public boolean hasSlaViolation(LocalDateTime deadline) {
        if (deadline == null) return false;
        return LocalDateTime.now().isAfter(deadline);
    }
    
    /**
     * Calculate business hours deadline (excluding weekends)
     */
    private LocalDateTime calculateBusinessDeadline(LocalDateTime startTime, int businessHours) {
        LocalDateTime deadline = startTime;
        int hoursAdded = 0;
        
        while (hoursAdded < businessHours) {
            deadline = deadline.plusHours(1);
            
            // Skip weekends (Saturday = 6, Sunday = 7)
            if (deadline.getDayOfWeek().getValue() < 6) {
                hoursAdded++;
            }
        }
        
        return deadline;
    }
    
    /**
     * Get SLA configuration from environment config
     */
    private JsonNode getSlaConfiguration() throws JsonProcessingException {
        Query query = new Query(Criteria.where("name").is(SLA_CONFIG_KEY));
        Map<String, Object> config = mongoTemplate.findOne(query, Map.class, "cm_environment_config");
        
        if (config == null) {
            log.warn("SLA configuration not found, using defaults");
            return createDefaultSlaConfig();
        }
        
        String configValue = (String) config.get("value");
        return objectMapper.readTree(configValue);
    }
    
    /**
     * Create default SLA configuration
     */
    private JsonNode createDefaultSlaConfig() throws JsonProcessingException {
        String defaultConfig = """
            {
                "defaultSlaHours": 72,
                "step_1": {"slaHours": 24, "description": "Initial Review"},
                "step_2": {"slaHours": 48, "description": "Investigation"},
                "step_3": {"slaHours": 24, "description": "Decision"},
                "alertType_AML": {"defaultStepSlaHours": 48},
                "alertType_FRAUD": {"defaultStepSlaHours": 24}
            }
            """;
        return objectMapper.readTree(defaultConfig);
    }
}
