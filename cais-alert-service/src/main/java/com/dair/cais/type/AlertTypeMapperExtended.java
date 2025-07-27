package com.dair.cais.type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AlertTypeMapperExtended {

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson object mapper for JSON operations

    public AlertTypeExtended toModel(AlertTypeEntityExtended entity) {
        AlertTypeExtended type = new AlertTypeExtended();
        type.setAlertTypeId(entity.getAlertTypeId());
        type.setTypeSlug(entity.getTypeSlug());
        type.setTypeName(entity.getTypeName());
        type.setDescription(entity.getDescription());
        type.setActive(entity.isActive());
        type.setExtraField(entity.getExtraField());

        // Deserialize JSON strings into List<Map<String, Object>>
        type.setField_schema(convertListToJsonString(entity.getField_schema()));
        type.setMandatory_fields(convertListToJsonString(entity.getMandatory_fields()));

        type.setCreatedAt(entity.getCreatedAt());
        type.setUpdatedAt(entity.getUpdatedAt());
        type.setWorkflowId(entity.getWorkflowId());

        return type;
    }

    private void mapAlertTypeToEntity(AlertTypeExtended alertType, AlertTypeEntityExtended entity) {
        entity.setAlertTypeId(alertType.getAlertTypeId());
        entity.setTypeSlug(alertType.getTypeSlug());
        entity.setTypeName(alertType.getTypeName());
        entity.setDescription(alertType.getDescription());
        entity.setActive(alertType.isActive());
        entity.setExtraField(alertType.getExtraField());

        // Convert List<Map<String, Object>> back to JSON string for storage
        entity.setField_schema(alertType.getField_schema());
        entity.setMandatory_fields(alertType.getMandatory_fields());

        entity.setCreatedAt(alertType.getCreatedAt());
        entity.setUpdatedAt(alertType.getUpdatedAt());
        //entity.set(alertType.getWorkflowId());
    }

    public AlertTypeEntityExtended toEntity(AlertTypeExtended type) {
        AlertTypeEntityExtended typeEntity = new AlertTypeEntityExtended();
        mapAlertTypeToEntity(type, typeEntity);

        return typeEntity;
    }

    // Utility method to convert JSON string to List<Map<String, Object>>
    private List<Map<String, Object>> convertJsonStringToList(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing JSON string to List<Map<String, Object>>", e);
        }
    }

    // Utility method to convert List<Map<String, Object>> to JSON string
    public String convertListToJsonString(List<Map<String, Object>> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing List<Map<String, Object>> to JSON string", e);
        }
    }
}






//package com.dair.cais.type;
//
//import org.springframework.stereotype.Component;
//
//@Component
//public class AlertTypeMapperExtended {
//    public AlertTypeExtended toModel(AlertTypeEntityExtended entity) {
//        AlertTypeExtended type = new AlertTypeExtended();
//        type.setAlertTypeId(entity.getAlertTypeId());
//        type.setTypeSlug(entity.getTypeSlug());
//        type.setTypeName(entity.getTypeName());
//        type.setDescription(entity.getDescription());
//        type.setActive(entity.isActive());
//        type.setExtraField(entity.getExtraField());
//        type.setField_schema(entity.getField_schema());
//        type.setMandatory_fields(entity.getMandatory_fields());
//        type.setCreatedAt(entity.getCreatedAt());
//        type.setUpdatedAt(entity.getUpdatedAt());
//
//        return type;
//    }
//
//    private void mapAlertTypeToEntity(AlertTypeExtended alertType, AlertTypeEntityExtended entity) {
//        entity.setAlertTypeId(alertType.getAlertTypeId());
//        entity.setTypeSlug(alertType.getTypeSlug());
//        entity.setTypeName(alertType.getTypeName());
//        entity.setDescription(alertType.getDescription());
//        entity.setActive(alertType.isActive());
//        entity.setExtraField(alertType.getExtraField());
//        entity.setField_schema(alertType.getField_schema());
//        entity.setMandatory_fields(alertType.getMandatory_fields());
//        entity.setCreatedAt(alertType.getCreatedAt());
//        entity.setUpdatedAt(alertType.getUpdatedAt());
//    }
//
//    public AlertTypeEntityExtended toEntity(AlertTypeExtended type) {
//        AlertTypeEntityExtended typeEntity = new AlertTypeEntityExtended();
//        mapAlertTypeToEntity(type, typeEntity);
//
//        return typeEntity;
//    }
//}
