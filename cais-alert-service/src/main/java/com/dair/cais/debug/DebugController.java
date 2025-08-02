package com.dair.cais.debug;

import com.dair.cais.alert.AlertEntity;
import com.dair.cais.alert.AlertRepository;
import com.dair.cais.type.AlertTypeExtended;
import com.dair.cais.type.AlertTypeServiceExtended;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertTypeServiceExtended alertTypeServiceExtended;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/alert/{alertId}")
    public ResponseEntity<Map<String, Object>> getAlertDebugInfo(@PathVariable String alertId) {
        log.info("DEBUG: Getting alert info for ID: {}", alertId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get alert from database
            AlertEntity alert = alertRepository.findByAlertId(alertId);
            
            if (alert == null) {
                response.put("status", "Alert not found");
                response.put("alertId", alertId);
                return ResponseEntity.ok(response);
            }
            
            // Basic alert info
            response.put("status", "Alert found");
            response.put("alertId", alert.getAlertId());
            response.put("alertTypeId", alert.getAlertTypeId());
            response.put("alertStepId", alert.getAlertStepId());
            response.put("createDate", alert.getCreateDate());
            
            // Get alert type info
            if (alert.getAlertTypeId() != null) {
                try {
                    AlertTypeExtended alertType = alertTypeServiceExtended.getAlertTypeFields(alert.getAlertTypeId());
                    if (alertType != null) {
                        Map<String, Object> typeInfo = new HashMap<>();
                        typeInfo.put("typeName", alertType.getTypeName());
                        typeInfo.put("workflowId", alertType.getWorkflowId());
                        typeInfo.put("isActive", alertType.isActive());
                        response.put("alertType", typeInfo);
                    }
                } catch (Exception e) {
                    response.put("alertTypeError", e.getMessage());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting alert debug info", e);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @Autowired
    private javax.sql.DataSource dataSource;

    @GetMapping("/test-db")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        log.info("DEBUG: Testing database connection");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test MongoDB
            long alertCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), AlertEntity.class, "alerts");
            response.put("mongodb_status", "Connected");
            response.put("totalAlerts", alertCount);
            
            // Test PostgreSQL
            try (java.sql.Connection conn = dataSource.getConnection()) {
                response.put("postgresql_status", "Connected");
                response.put("postgresql_url", conn.getMetaData().getURL());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Database connection test failed", e);
            response.put("status", "Database connection failed");
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/sql")
    public ResponseEntity<Map<String, Object>> executeSqlQuery(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        log.info("DEBUG: Executing SQL query: {}", sql);
        
        Map<String, Object> response = new HashMap<>();
        
        if (sql == null || sql.trim().isEmpty()) {
            response.put("error", "SQL query is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            boolean isResultSet = stmt.execute(sql);
            
            if (isResultSet) {
                try (java.sql.ResultSet rs = stmt.getResultSet()) {
                    java.sql.ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    java.util.List<String> columns = new java.util.ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metaData.getColumnName(i));
                    }
                    
                    java.util.List<Map<String, Object>> rows = new java.util.ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        rows.add(row);
                    }
                    
                    response.put("columns", columns);
                    response.put("rows", rows);
                    response.put("rowCount", rows.size());
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                response.put("updateCount", updateCount);
            }
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error executing SQL query", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/update-alert-step")
    public ResponseEntity<Map<String, Object>> updateAlertStep(@RequestBody Map<String, String> request) {
        String alertId = request.get("alertId");
        String newStepId = request.get("alertStepId");
        
        log.info("DEBUG: Updating alert step for alert ID: {} to step: {}", alertId, newStepId);
        
        Map<String, Object> response = new HashMap<>();
        
        if (alertId == null || alertId.trim().isEmpty()) {
            response.put("error", "alertId is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (newStepId == null || newStepId.trim().isEmpty()) {
            response.put("error", "alertStepId is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Update MongoDB
            org.springframework.data.mongodb.core.query.Query mongoQuery = 
                new org.springframework.data.mongodb.core.query.Query(
                    org.springframework.data.mongodb.core.query.Criteria.where("alertId").is(alertId));
            
            org.springframework.data.mongodb.core.query.Update mongoUpdate = 
                new org.springframework.data.mongodb.core.query.Update().set("alertStepId", newStepId);
            
            mongoTemplate.updateFirst(mongoQuery, mongoUpdate, AlertEntity.class, "alerts");
            
            response.put("success", true);
            response.put("message", "Alert step updated successfully");
            response.put("alertId", alertId);
            response.put("newStepId", newStepId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating alert step", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}