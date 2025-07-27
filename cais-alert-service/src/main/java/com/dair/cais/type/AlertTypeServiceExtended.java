package com.dair.cais.type;

import com.dair.exception.CaisBaseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlertTypeServiceExtended {

    @Autowired
    private AlertTypeMapperExtended typeMapper;

    @Autowired
    private AlertTypeMapperExtended typeMapperExtended;

    @Autowired
    private AlertTypeRepositoryExtended typeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AlertTypeExtended getAlertTypeFields(final String alertTypeId) {
        log.debug("Fetching alert type fields for ID: {}", alertTypeId);

        try {
            AlertTypeEntityExtended typeById = typeRepository.getAlertTypeFields(alertTypeId);
            if (typeById == null) {
                log.error("Alert type not found with ID: {}", alertTypeId);
                throw new EntityNotFoundException("Alert type not found with ID: " + alertTypeId);
            }

            return typeMapper.toModel(typeById);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching alert type fields for ID: {}", alertTypeId, e);
            throw new CaisBaseException("Error retrieving alert type fields: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> fetchalertTypesAll() {
        log.debug("Fetching all alert types");

        try {
            List<AlertTypeEntityExtended> allAlertTypeEntities = typeRepository.fetchAllAlertTypes();
            log.debug("Found {} alert types", allAlertTypeEntities.size());

            List<AlertTypeExtended> allAlertTypes = allAlertTypeEntities.stream()
                    .map(typeMapperExtended::toModel)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("types", allAlertTypes);
            response.put("count", allAlertTypes.size());
            return response;
        } catch (Exception e) {
            log.error("Error fetching all alert types", e);
            throw new CaisBaseException("Error retrieving types: " + e.getMessage());
        }
    }

    /**
     * Gets the workflow configuration for an alert type.
     * @param alertTypeId the alert type ID
     * @return the workflow configuration as a JsonNode
     * @throws CaisBaseException if the configuration is invalid or missing
     */
    public JsonNode getWorkflowConfiguration(String alertTypeId) {
        log.debug("Getting workflow configuration for alert type: {}", alertTypeId);

        try {
            AlertTypeExtended alertType = getAlertTypeFields(alertTypeId);
            if (alertType.getField_schema() == null || alertType.getField_schema().isEmpty()) {
                log.error("No workflow configuration found for alert type: {}", alertTypeId);
                throw new EntityNotFoundException("No workflow configuration found for alert type: " + alertTypeId);
            }

            // Convert the field_schema back to JSON string
            String jsonString = objectMapper.writeValueAsString(alertType.getField_schema());
            return objectMapper.readTree(jsonString);

        } catch (JsonProcessingException e) {
            log.error("Error parsing workflow configuration for alert type: {}", alertTypeId, e);
            throw new CaisBaseException("Error parsing workflow configuration: " + e.getMessage());
        }
    }
}