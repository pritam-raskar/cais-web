package com.dair.cais.connector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/connectors")
public class ConnectorController {

    private final ConnectorService connectorService;

    @Autowired
    public ConnectorController(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    @PostMapping
    public ResponseEntity<Connector> createConnector(@RequestBody Connector connector) {
        return ResponseEntity.ok(connectorService.saveConnector(connector));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Connector> getConnector(@PathVariable Long id) {
        return connectorService.getConnector(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Connector>> getAllConnectors() {
        return ResponseEntity.ok(connectorService.getAllConnectors());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnector(@PathVariable Long id) {
        connectorService.deleteConnector(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Boolean> testConnection(@RequestBody Connector connector) {
        return ResponseEntity.ok(connectorService.testConnection(connector));
    }
}
