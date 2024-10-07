package com.dair.cais.alert.checklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alert-checklists")
public class AlertStepChecklistController {

    private final AlertStepChecklistService alertStepChecklistService;

    @Autowired
    public AlertStepChecklistController(AlertStepChecklistService alertStepChecklistService) {
        this.alertStepChecklistService = alertStepChecklistService;
    }

    @PostMapping
    public ResponseEntity<AlertStepChecklist> saveAlertChecklist(@RequestBody AlertStepChecklist alertStepChecklist) {
        AlertStepChecklist savedChecklist = alertStepChecklistService.saveAlertStepChecklist(alertStepChecklist);
        return ResponseEntity.ok(savedChecklist);
    }
}