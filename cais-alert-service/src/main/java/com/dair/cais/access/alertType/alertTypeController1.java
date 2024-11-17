package com.dair.cais.access.alertType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alert-types")
public class alertTypeController1 {

    @Autowired
    private alertTypeService1 alertTypeService;


    @GetMapping("")
    public ResponseEntity<List<alertType>> getAllAlertTypes() {
        List<alertType> alertTypes = alertTypeService.getAllAlertTypes();
        return ResponseEntity.ok(alertTypes);
    }

    @GetMapping("/{alertTypeid}")
    public ResponseEntity<alertType> getAlertTypeById(@PathVariable String alertTypeid) {
        alertType alertType = alertTypeService.getAlertTypeById(alertTypeid);
        return ResponseEntity.ok(alertType);
    }


    // Create a new alert type
    @PostMapping("/create")
    public ResponseEntity<alertType> createAlertType(@RequestBody alertType alertType) {
        alertType createdAlertType = alertTypeService.createAlertType(alertType);
        return ResponseEntity.ok(createdAlertType);
    }

    // Delete an alert type by ID
    @DeleteMapping("/delete/{alertTypeId}")
    public ResponseEntity<alertType> deleteAlertType(@PathVariable String alertTypeId) {
        alertType alertType  = alertTypeService.deleteAlertType(alertTypeId);
        return ResponseEntity.ok(alertType);
    }

    // Update an alert type by ID
    @PutMapping("/update/{alertTypeId}")
    public ResponseEntity<alertType> updateAlertType(
            @PathVariable() String alertTypeId, @RequestBody alertType updatedAlertType) {
        alertType alertType = alertTypeService.updateAlertType(alertTypeId, updatedAlertType);
        return ResponseEntity.ok(alertType);
    }
}
