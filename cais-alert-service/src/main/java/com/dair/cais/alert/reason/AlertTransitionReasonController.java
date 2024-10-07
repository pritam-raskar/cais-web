package com.dair.cais.alert.reason;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alert-transition-reasons")
public class AlertTransitionReasonController {

    private final AlertTransitionReasonService alertTransitionReasonService;

    @Autowired
    public AlertTransitionReasonController(AlertTransitionReasonService alertTransitionReasonService) {
        this.alertTransitionReasonService = alertTransitionReasonService;
    }

    @PostMapping
    public ResponseEntity<AlertTransitionReason> saveAlertTransitionReason(@RequestBody AlertTransitionReason alertTransitionReason) {
        AlertTransitionReason savedReason = alertTransitionReasonService.saveAlertTransitionReason(alertTransitionReason);
        return ResponseEntity.ok(savedReason);
    }
}