package com.dair.cais.enrichment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data/umbrella")
public class EnrichAlertDataController {

    @Autowired
    private EnrichmentAlertDataService umbrellaService;

    @GetMapping("/query")
    public ResponseEntity<List<Map<String, Object>>> queryData(
            @RequestParam String fieldName,
            @RequestParam String schema,
            @RequestParam String tableName,
            @RequestParam String columnName,
            @RequestParam String alertId) {
        List<Map<String, Object>> result = umbrellaService.queryData(fieldName, schema, tableName, columnName, alertId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/execute")
    public ResponseEntity<List<Map<String, Object>>> executeFlexibleQuery(
            @RequestParam Long connectorId,
            @RequestBody QueryExecutionRequest executionRequest) {
        List<Map<String, Object>> result = umbrellaService.executeFlexibleQuery(
                connectorId,
                executionRequest.getQuery(),
                executionRequest.getParameters() // This can be either a List or a Map
        );
        return ResponseEntity.ok(result);
    }
}


