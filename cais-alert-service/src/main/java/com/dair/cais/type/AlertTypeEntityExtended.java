package com.dair.cais.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(collection = "alertTypes")
public class AlertTypeEntityExtended {

    private String alertTypeId;
    private String typeName;
    private String typeSlug;
    private String description;
    private boolean isActive;
    private List<String> extraField;
    private String field_schema; // Store as JSON string
    private String mandatory_fields; // Store as JSON string
    private Date createdAt;
    private Date updatedAt;

    private static final ObjectMapper objectMapper = new ObjectMapper(); // Reuse ObjectMapper

    // Getters and Setters

    public String getAlertTypeId() { return alertTypeId; }
    public void setAlertTypeId(String alertTypeId) { this.alertTypeId = alertTypeId; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getTypeSlug() { return typeSlug; }
    public void setTypeSlug(String typeSlug) { this.typeSlug = typeSlug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public List<String> getExtraField() { return extraField; }
    public void setExtraField(List<String> extraField) { this.extraField = extraField; }

    // Updated getter for field_schema to return List<Map<String, Object>>
    public List<Map<String, Object>> getField_schema() {
        try {
            return objectMapper.readValue(field_schema, List.class);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing field_schema", e);
        }
    }

    // Updated setter for field_schema (optional)
    public void setField_schema(String field_schema) { this.field_schema = field_schema; }

    // Updated getter for mandatory_fields to return List<Map<String, Object>>
    public List<Map<String, Object>> getMandatory_fields() {
        try {
            return objectMapper.readValue(mandatory_fields, List.class);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing mandatory_fields", e);
        }
    }

    public void setMandatory_fields(String mandatory_fields) { this.mandatory_fields = mandatory_fields; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}







//package com.dair.cais.type;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.io.IOException;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
//@Document(collection = "alertTypes")
//public class AlertTypeEntityExtended {
//
//    private String alertTypeId;
//    private String typeName;
//    private String typeSlug;
//    private String description;
//    private boolean isActive;
//    private List<String> extraField;
//    private String field_schema; // Store as JSON string
//    private String mandatory_fields; // Store as JSON string
//    private Date createdAt;
//    private Date updatedAt;
//
//    // Getters and Setters
//
//    public String getAlertTypeId() { return alertTypeId; }
//    public void setAlertTypeId(String alertTypeId) { this.alertTypeId = alertTypeId; }
//    public String getTypeName() { return typeName; }
//    public void setTypeName(String typeName) { this.typeName = typeName; }
//    public String getTypeSlug() { return typeSlug; }
//    public void setTypeSlug(String typeSlug) { this.typeSlug = typeSlug; }
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//    public boolean isActive() { return isActive; }
//    public void setActive(boolean active) { isActive = active; }
//    public List<String> getExtraField() { return extraField; }
//    public void setExtraField(List<String> extraField) { this.extraField = extraField; }
//    public String getField_schema() { return field_schema; }
//    public void setField_schema(String field_schema) { this.field_schema = field_schema; }
//    public String getMandatory_fields() { return mandatory_fields; }
//    public void setMandatory_fields(String mandatory_fields) { this.mandatory_fields = mandatory_fields; }
//    public Date getCreatedAt() { return createdAt; }
//    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
//    public Date getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
//
//    public List<Map<String, Object>> getFieldSchemaParsed() {
//        return parseJsonStringToList(field_schema);
//    }
//
//    public List<Map<String, Object>> getMandatoryFieldsParsed() {
//        return parseJsonStringToList(mandatory_fields);
//    }
//
//    private List<Map<String, Object>> parseJsonStringToList(String jsonString) {
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            return mapper.readValue(jsonString, new TypeReference<List<Map<String, Object>>>() {});
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//}
